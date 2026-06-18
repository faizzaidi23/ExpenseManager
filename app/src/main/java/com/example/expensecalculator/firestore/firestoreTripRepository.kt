package com.example.expensecalculator.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// ─── SETTLEMENT DATA CLASS (UPDATED FOR UIDs) ────────────────────────────────
data class FirestoreSettlement(
    val id: String = "",
    val fromUid: String = "",
    val toUid: String = "",
    val fromName: String = "",
    val toName: String = "",
    val amount: Double = 0.0,
    val status: String = "pending", // Can be "pending" or "confirmed"
    val timestamp: Long = 0L
)

class FirestoreTripRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val uid get() = auth.currentUser?.uid ?: ""
    private val currentUser get() = auth.currentUser

    // ─── TRIPS ───────────────────────────────────────────────────────────────

    fun getMyTrips(): Flow<List<FirestoreTrip>> = callbackFlow {
        val listener: ListenerRegistration = db.collection("trips")
            .whereArrayContains("participantUids", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val trips = snapshot?.documents?.mapNotNull { doc -> doc.toFirestoreTrip() } ?: emptyList()
                trySend(trips)
            }
        awaitClose { listener.remove() }
    }

    fun getTripById(tripId: String): Flow<FirestoreTrip?> = callbackFlow {
        val listener = db.collection("trips").document(tripId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(null); return@addSnapshotListener }
                trySend(snapshot?.toFirestoreTrip())
            }
        awaitClose { listener.remove() }
    }

    suspend fun createTrip(title: String, currency: String): String {
        val creatorName = currentUser?.displayName ?: "Unknown"
        val creator = FirestoreParticipant(uid = uid, name = creatorName, email = currentUser?.email ?: "")
        val tripData = hashMapOf(
            "title" to title,
            "currency" to currency,
            "createdBy" to uid,
            "createdByName" to creatorName,
            "participants" to listOf(mapOf("uid" to creator.uid, "name" to creator.name, "email" to creator.email)),
            "participantUids" to listOf(uid)
        )
        return db.collection("trips").add(tripData).await().id
    }

    suspend fun deleteTrip(tripId: String) {
        db.collection("trips").document(tripId).delete().await()
    }

    // ─── EXPENSES ────────────────────────────────────────────────────────────

    fun getExpensesForTrip(tripId: String): Flow<List<FirestoreExpense>> = callbackFlow {
        val listener = db.collection("trips").document(tripId)
            .collection("expenses")
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val expenses = snapshot?.documents?.mapNotNull { doc -> doc.toFirestoreExpense() } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addExpense(
        tripId: String, expenseName: String, amount: Double,
        paidByUid: String, paidByName: String,
        splits: List<FirestoreExpenseSplit>, categoryName: String? = null
    ) {
        val expenseData = hashMapOf(
            "tripId" to tripId, "expenseName" to expenseName, "amount" to amount,
            "paidByUid" to paidByUid, "paidByName" to paidByName,
            "date" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            "splits" to splits.map { mapOf("uid" to it.uid, "name" to it.name, "shareAmount" to it.shareAmount) },
            "categoryName" to categoryName
        )
        db.collection("trips").document(tripId).collection("expenses").add(expenseData).await()
        sendNotificationToParticipants(tripId, "expense_added", "${currentUser?.displayName ?: "Someone"} added expense '$expenseName'")
    }

    suspend fun deleteExpense(tripId: String, expenseId: String, expenseName: String) {
        db.collection("trips").document(tripId).collection("expenses").document(expenseId).delete().await()
        sendNotificationToParticipants(tripId, "expense_deleted", "${currentUser?.displayName ?: "Someone"} deleted expense '$expenseName'")
    }

    // ─── USER SEARCH & INVITES ───────────────────────────────────────────────

    suspend fun searchUsers(query: String): List<FirestoreUser> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()

        val byEmail = db.collection("users").whereEqualTo("email", q).get().await()
            .documents.mapNotNull { it.toFirestoreUser() }.filter { it.uid != uid }
        if (byEmail.isNotEmpty()) return byEmail

        return db.collection("users").orderBy("name").startAt(query.trim()).endAt(query.trim() + "\uf8ff")
            .get().await().documents.mapNotNull { it.toFirestoreUser() }.filter { it.uid != uid }
    }

    suspend fun sendInvite(trip: FirestoreTrip, toUser: FirestoreUser) {
        if (trip.participants.any { it.uid == toUser.uid }) return
        val existing = db.collection("trip_invites").whereEqualTo("tripId", trip.id).whereEqualTo("fromUid", uid)
            .whereEqualTo("toUid", toUser.uid).whereEqualTo("status", "pending").get().await()
        if (!existing.isEmpty) return

        val inviteData = hashMapOf(
            "tripId" to trip.id, "tripTitle" to trip.title,
            "fromUid" to uid, "fromName" to (currentUser?.displayName ?: "Unknown"),
            "toUid" to toUser.uid, "toEmail" to toUser.email, "toName" to toUser.name,
            "status" to "pending", "timestamp" to System.currentTimeMillis()
        )
        db.collection("trip_invites").add(inviteData).await()
    }

    fun getMyPendingInvites(): Flow<List<FirestoreInvite>> = callbackFlow {
        val listener = db.collection("trip_invites").whereEqualTo("toUid", uid).whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                trySend(snapshot?.documents?.mapNotNull { it.toFirestoreInvite() } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun acceptInvite(invite: FirestoreInvite) {
        val batch = db.batch()
        batch.update(db.collection("trip_invites").document(invite.id), "status", "accepted")

        val tripRef = db.collection("trips").document(invite.tripId)
        val newParticipant = mapOf(
            "uid" to uid, "name" to (currentUser?.displayName ?: invite.toName), "email" to (currentUser?.email ?: invite.toEmail)
        )
        batch.update(tripRef, "participantUids", FieldValue.arrayUnion(uid))
        batch.update(tripRef, "participants", FieldValue.arrayUnion(newParticipant))
        batch.commit().await()
        sendNotificationToParticipants(invite.tripId, "member_joined", "${currentUser?.displayName ?: "Someone"} joined the trip")
    }

    suspend fun declineInvite(invite: FirestoreInvite) {
        db.collection("trip_invites").document(invite.id).update("status", "declined").await()
    }

    // ─── CATEGORIES ──────────────────────────────────────────────────────────

    fun getCategoriesForTrip(tripId: String): Flow<List<FirestoreCategory>> = callbackFlow {
        val listener = db.collection("trips").document(tripId).collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                trySend(snapshot?.documents?.mapNotNull { it.toFirestoreCategory() } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun addCategory(tripId: String, name: String, iconName: String) {
        db.collection("trips").document(tripId).collection("categories").add(hashMapOf("name" to name, "iconName" to iconName)).await()
        sendNotificationToParticipants(tripId, "category_added", "${currentUser?.displayName ?: "Someone"} added category '$name'")
    }

    suspend fun deleteCategory(tripId: String, categoryId: String, categoryName: String) {
        db.collection("trips").document(tripId).collection("categories").document(categoryId).delete().await()
        sendNotificationToParticipants(tripId, "category_deleted", "${currentUser?.displayName ?: "Someone"} deleted category '$categoryName'")
    }

    // ─── NOTIFICATIONS ────────────────────────────────────────────────────────

    private suspend fun sendNotificationToParticipants(tripId: String, type: String, message: String) {
        val trip = db.collection("trips").document(tripId).get().await()
        val tripTitle = trip.getString("title") ?: ""
        val participantsList = trip.get("participants") as? List<Map<String, Any>> ?: return
        val actorName = currentUser?.displayName ?: "Someone"

        participantsList.forEach { participant ->
            val recipientUid = participant["uid"] as? String ?: return@forEach
            val notif = hashMapOf(
                "tripId" to tripId, "tripTitle" to tripTitle, "actorUid" to uid, "actorName" to actorName,
                "recipientUid" to recipientUid, "type" to type, "message" to message,
                "timestamp" to System.currentTimeMillis(), "read" to false
            )
            db.collection("trip_notifications").add(notif).await()
        }
    }

    fun getMyNotifications(): Flow<List<FirestoreNotification>> = callbackFlow {
        val listener = db.collection("trip_notifications").whereEqualTo("recipientUid", uid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                trySend(snapshot?.documents?.mapNotNull { doc ->
                    try {
                        FirestoreNotification(
                            id = doc.id, tripId = doc.getString("tripId") ?: "", tripTitle = doc.getString("tripTitle") ?: "",
                            actorUid = doc.getString("actorUid") ?: "", actorName = doc.getString("actorName") ?: "",
                            recipientUid = doc.getString("recipientUid") ?: "", type = doc.getString("type") ?: "",
                            message = doc.getString("message") ?: "", timestamp = (doc.get("timestamp") as? Number)?.toLong() ?: 0L,
                            read = doc.getBoolean("read") ?: false
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun deleteNotification(notifId: String) = db.collection("trip_notifications").document(notifId).delete().await()

    suspend fun markAllNotificationsRead() {
        val batch = db.batch()
        db.collection("trip_notifications").whereEqualTo("recipientUid", uid).whereEqualTo("read", false)
            .get().await().documents.forEach { batch.update(it.reference, "read", true) }
        batch.commit().await()
    }

    // ─── SETTLEMENTS (UPDATED FOR UIDs & SPAM PREVENTION) ─────────────────────

    suspend fun initiateSettlement(tripId: String, fromUid: String, toUid: String, fromName: String, toName: String, amount: Double) {
        // Spam prevention logic
        val existingRequest = db.collection("trips").document(tripId)
            .collection("paid_settlements")
            .whereEqualTo("fromUid", fromUid)
            .whereEqualTo("toUid", toUid)
            .whereEqualTo("status", "pending")
            .get()
            .await()

        if (!existingRequest.isEmpty) {
            throw Exception("A pending request has already been sent to $toName.")
        }

        val settlementData = hashMapOf(
            "fromUid" to fromUid,
            "toUid" to toUid,
            "fromName" to fromName,
            "toName" to toName,
            "amount" to amount,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("trips").document(tripId).collection("paid_settlements").add(settlementData).await()

        // FIXED: Added the "Waiting for confirmation." text back into the notification
        sendNotificationToParticipants(
            tripId = tripId,
            type = "settlement_pending",
            message = "$fromName marked ₹${"%.2f".format(amount)} to $toName as paid. Waiting for confirmation."
        )
    }

    suspend fun confirmSettlement(tripId: String, settlementId: String, toName: String) {
        db.collection("trips").document(tripId).collection("paid_settlements").document(settlementId).update("status", "confirmed").await()
        sendNotificationToParticipants(tripId, "settlement_confirmed", "$toName confirmed receiving the payment!")
    }

    suspend fun rejectSettlement(tripId: String, settlementId: String) {
        db.collection("trips").document(tripId).collection("paid_settlements").document(settlementId).delete().await()
    }

    fun getPaidSettlements(tripId: String): Flow<List<FirestoreSettlement>> = callbackFlow {
        val listener = db.collection("trips").document(tripId).collection("paid_settlements")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                trySend(snapshot?.documents?.mapNotNull { doc ->
                    try {
                        FirestoreSettlement(
                            id = doc.id,
                            fromUid = doc.getString("fromUid") ?: "",
                            toUid = doc.getString("toUid") ?: "",
                            fromName = doc.getString("fromName") ?: "",
                            toName = doc.getString("toName") ?: "",
                            amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0,
                            status = doc.getString("status") ?: "pending",
                            timestamp = (doc.get("timestamp") as? Number)?.toLong() ?: 0L
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    // ─── GLOBAL STATISTICS (UPDATED FOR UIDs) ────────────────────────────────

    data class TripBalanceSummary(
        val tripId: String, val tripTitle: String, val payables: Double, val receivables: Double, val netBalance: Double
    )

    suspend fun calculateTripBalances(trips: List<FirestoreTrip>): List<TripBalanceSummary> {
        val summaries = mutableListOf<TripBalanceSummary>()
        val userUid = currentUser?.uid ?: return emptyList()

        for (trip in trips) {
            try {
                var tripBalance = 0.0

                val expensesSnapshot = db.collection("trips").document(trip.id).collection("expenses").get().await()
                val expenses = expensesSnapshot.documents.mapNotNull { it.toFirestoreExpense() }

                val settlementsSnapshot = db.collection("trips").document(trip.id).collection("paid_settlements").get().await()

                expenses.forEach { expense ->
                    if (expense.paidByUid == userUid) tripBalance += expense.amount
                    expense.splits.forEach { split ->
                        if (split.uid == userUid) tripBalance -= split.shareAmount
                    }
                }

                settlementsSnapshot.documents.forEach { doc ->
                    if (doc.getString("status") == "confirmed") {
                        val fromUid = doc.getString("fromUid") ?: ""
                        val toUid = doc.getString("toUid") ?: ""
                        val amt = (doc.get("amount") as? Number)?.toDouble() ?: 0.0

                        if (fromUid == userUid) tripBalance += amt
                        if (toUid == userUid) tripBalance -= amt
                    }
                }

                var payables = 0.0
                var receivables = 0.0
                if (tripBalance > 0.01) receivables = tripBalance else if (tripBalance < -0.01) payables = kotlin.math.abs(tripBalance)

                if (expenses.isNotEmpty() || payables > 0 || receivables > 0) {
                    summaries.add(TripBalanceSummary(trip.id, trip.title, payables, receivables, tripBalance))
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
        return summaries.sortedByDescending { kotlin.math.abs(it.netBalance) }
    }

    // ─── PARSING HELPERS ──────────────────────────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toFirestoreTrip(): FirestoreTrip? {
        return try {
            val participantsList = get("participants") as? List<Map<String, Any>> ?: emptyList()
            FirestoreTrip(
                id = id, title = getString("title") ?: return null, currency = getString("currency") ?: "INR",
                createdBy = getString("createdBy") ?: "", createdByName = getString("createdByName") ?: "",
                participants = participantsList.map { FirestoreParticipant(uid = it["uid"] as? String ?: "", name = it["name"] as? String ?: "", email = it["email"] as? String ?: "") }
            )
        } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFirestoreExpense(): FirestoreExpense? {
        return try {
            val splitsList = get("splits") as? List<Map<String, Any>> ?: emptyList()
            FirestoreExpense(
                id = id, tripId = getString("tripId") ?: "", expenseName = getString("expenseName") ?: return null,
                amount = (get("amount") as? Number)?.toDouble() ?: 0.0, paidByUid = getString("paidByUid") ?: "",
                paidByName = getString("paidByName") ?: "", date = getString("date") ?: "",
                splits = splitsList.map { FirestoreExpenseSplit(uid = it["uid"] as? String ?: "", name = it["name"] as? String ?: "", shareAmount = (it["shareAmount"] as? Number)?.toDouble() ?: 0.0) },
                categoryName = getString("categoryName"), categoryIconName = getString("categoryIconName")
            )
        } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFirestoreUser(): FirestoreUser? {
        return try { FirestoreUser(uid = id, name = getString("name") ?: return null, email = getString("email") ?: "") } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFirestoreInvite(): FirestoreInvite? {
        return try {
            FirestoreInvite(
                id = id, tripId = getString("tripId") ?: return null, tripTitle = getString("tripTitle") ?: "",
                fromUid = getString("fromUid") ?: "", fromName = getString("fromName") ?: "",
                toUid = getString("toUid") ?: "", toEmail = getString("toEmail") ?: "",
                toName = getString("toName") ?: "", status = getString("status") ?: "pending",
                timestamp = (get("timestamp") as? Number)?.toLong() ?: 0L
            )
        } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFirestoreCategory(): FirestoreCategory? {
        return try { FirestoreCategory(id = id, name = getString("name") ?: return null, iconName = getString("iconName") ?: "category") } catch (e: Exception) { null }
    }
}