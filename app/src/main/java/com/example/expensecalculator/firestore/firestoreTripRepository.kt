package com.example.expensecalculator.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreTripRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val uid get() = auth.currentUser?.uid ?: ""
    private val currentUser get() = auth.currentUser

    // ─── TRIPS ───────────────────────────────────────────────────────────────

    // Returns trips where current user is a participant OR creator
    fun getMyTrips(): Flow<List<FirestoreTrip>> = callbackFlow {
        val listener: ListenerRegistration = db.collection("trips")
            .whereArrayContains("participantUids", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val trips = snapshot?.documents?.mapNotNull { doc ->
                    doc.toFirestoreTrip()
                } ?: emptyList()
                trySend(trips)
            }
        awaitClose { listener.remove() }
    }

    fun getTripById(tripId: String): Flow<FirestoreTrip?> = callbackFlow {
        val listener = db.collection("trips").document(tripId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toFirestoreTrip())
            }
        awaitClose { listener.remove() }
    }

    suspend fun createTrip(title: String, currency: String): String {
        val creatorName = currentUser?.displayName ?: "Unknown"
        val creator = FirestoreParticipant(
            uid = uid,
            name = creatorName,
            email = currentUser?.email ?: ""
        )
        val tripData = hashMapOf(
            "title" to title,
            "currency" to currency,
            "createdBy" to uid,
            "createdByName" to creatorName,
            "participants" to listOf(
                mapOf("uid" to creator.uid, "name" to creator.name, "email" to creator.email)
            ),
            // participantUids is a flat list used for Firestore array-contains queries
            "participantUids" to listOf(uid)
        )
        val docRef = db.collection("trips").add(tripData).await()
        return docRef.id
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
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    doc.toFirestoreExpense()
                } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addExpense(
        tripId: String,
        expenseName: String,
        amount: Double,
        paidByUid: String,
        paidByName: String,
        splits: List<FirestoreExpenseSplit>,
        categoryName: String? = null
    ) {
        val expenseData = hashMapOf(
            "tripId" to tripId,
            "expenseName" to expenseName,
            "amount" to amount,
            "paidByUid" to paidByUid,
            "paidByName" to paidByName,
            "date" to java.text.SimpleDateFormat(
                "yyyy-MM-dd", java.util.Locale.getDefault()
            ).format(java.util.Date()),
            "splits" to splits.map {
                mapOf("uid" to it.uid, "name" to it.name, "shareAmount" to it.shareAmount)
            },
            "categoryName" to categoryName
        )
        db.collection("trips").document(tripId)
            .collection("expenses")
            .add(expenseData)
            .await()
    }

    suspend fun deleteExpense(tripId: String, expenseId: String) {
        db.collection("trips").document(tripId)
            .collection("expenses")
            .document(expenseId)
            .delete()
            .await()
    }

    // ─── USER SEARCH ─────────────────────────────────────────────────────────

    suspend fun searchUsers(query: String): List<FirestoreUser> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()

        // Search by email first (exact match)
        val byEmail = db.collection("users")
            .whereEqualTo("email", q)
            .get().await()
            .documents.mapNotNull { it.toFirestoreUser() }
            .filter { it.uid != uid } // exclude self

        if (byEmail.isNotEmpty()) return byEmail

        // Search by name prefix
        val byName = db.collection("users")
            .orderBy("name")
            .startAt(query.trim())
            .endAt(query.trim() + "\uf8ff")
            .get().await()
            .documents.mapNotNull { it.toFirestoreUser() }
            .filter { it.uid != uid }

        return byName
    }

    // ─── INVITES ─────────────────────────────────────────────────────────────

    suspend fun sendInvite(trip: FirestoreTrip, toUser: FirestoreUser) {
        // Check if already a participant
        if (trip.participants.any { it.uid == toUser.uid }) return

        // Check if invite already pending
        val existing = db.collection("trip_invites")
            .whereEqualTo("tripId", trip.id)
            .whereEqualTo("fromUid",uid)
            .whereEqualTo("toUid", toUser.uid)
            .whereEqualTo("status", "pending")
            .get().await()
        if (!existing.isEmpty) return

        val inviteData = hashMapOf(
            "tripId" to trip.id,
            "tripTitle" to trip.title,
            "fromUid" to uid,
            "fromName" to (currentUser?.displayName ?: "Unknown"),
            "toUid" to toUser.uid,
            "toEmail" to toUser.email,
            "toName" to toUser.name,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("trip_invites").add(inviteData).await()
    }

    // Invites for current user (notification screen)
    fun getMyPendingInvites(): Flow<List<FirestoreInvite>> = callbackFlow {
        val listener = db.collection("trip_invites")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val invites = snapshot?.documents?.mapNotNull { doc ->
                    doc.toFirestoreInvite()
                } ?: emptyList()
                trySend(invites)
            }
        awaitClose { listener.remove() }
    }

    suspend fun acceptInvite(invite: FirestoreInvite) {
        val batch = db.batch()

        // 1. Update invite status
        val inviteRef = db.collection("trip_invites").document(invite.id)
        batch.update(inviteRef, "status", "accepted")

        // 2. Update trip — use FieldValue.arrayUnion to avoid reading first
        val tripRef = db.collection("trips").document(invite.tripId)
        val newParticipant = mapOf(
            "uid" to uid,
            "name" to (currentUser?.displayName ?: invite.toName),
            "email" to (currentUser?.email ?: invite.toEmail)
        )
        batch.update(tripRef, "participantUids", FieldValue.arrayUnion(uid))
        batch.update(tripRef, "participants", FieldValue.arrayUnion(newParticipant))

        batch.commit().await()
    }

    suspend fun declineInvite(invite: FirestoreInvite) {
        db.collection("trip_invites").document(invite.id)
            .update("status", "declined").await()
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toFirestoreTrip(): FirestoreTrip? {
        return try {
            val participantsList = get("participants") as? List<Map<String, Any>> ?: emptyList()
            FirestoreTrip(
                id = id,
                title = getString("title") ?: return null,
                currency = getString("currency") ?: "INR",
                createdBy = getString("createdBy") ?: "",
                createdByName = getString("createdByName") ?: "",
                participants = participantsList.map {
                    FirestoreParticipant(
                        uid = it["uid"] as? String ?: "",
                        name = it["name"] as? String ?: "",
                        email = it["email"] as? String ?: ""
                    )
                }
            )
        } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFirestoreExpense(): FirestoreExpense? {
        return try {
            val splitsList = get("splits") as? List<Map<String, Any>> ?: emptyList()
            FirestoreExpense(
                id = id,
                tripId = getString("tripId") ?: "",
                expenseName = getString("expenseName") ?: return null,
                amount = (get("amount") as? Number)?.toDouble() ?: 0.0,
                paidByUid = getString("paidByUid") ?: "",
                paidByName = getString("paidByName") ?: "",
                date = getString("date") ?: "",
                splits = splitsList.map {
                    FirestoreExpenseSplit(
                        uid = it["uid"] as? String ?: "",
                        name = it["name"] as? String ?: "",
                        shareAmount = (it["shareAmount"] as? Number)?.toDouble() ?: 0.0
                    )
                },
                categoryName = getString("categoryName")
            )
        } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFirestoreUser(): FirestoreUser? {
        return try {
            FirestoreUser(
                uid = id,
                name = getString("name") ?: return null,
                email = getString("email") ?: ""
            )
        } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFirestoreInvite(): FirestoreInvite? {
        return try {
            FirestoreInvite(
                id = id,
                tripId = getString("tripId") ?: return null,
                tripTitle = getString("tripTitle") ?: "",
                fromUid = getString("fromUid") ?: "",
                fromName = getString("fromName") ?: "",
                toUid = getString("toUid") ?: "",
                toEmail = getString("toEmail") ?: "",
                toName = getString("toName") ?: "",
                status = getString("status") ?: "pending",
                timestamp = (get("timestamp") as? Number)?.toLong() ?: 0L
            )
        } catch (e: Exception) { null }
    }
}