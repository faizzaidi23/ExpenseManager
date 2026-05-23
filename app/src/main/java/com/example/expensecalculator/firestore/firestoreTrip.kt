package com.example.expensecalculator.firestore

// Participant is now a real Firebase user
data class FirestoreParticipant(
    val uid: String = "",
    val name: String = "",
    val email: String = ""
)

data class FirestoreExpenseSplit(
    val uid: String = "",
    val name: String = "",
    val shareAmount: Double = 0.0
)

data class FirestoreExpense(
    val id: String = "",
    val tripId: String = "",
    val expenseName: String = "",
    val amount: Double = 0.0,
    val paidByUid: String = "",
    val paidByName: String = "",
    val date: String = "",
    val splits: List<FirestoreExpenseSplit> = emptyList(),
    val categoryName: String? = null,
    val categoryIconName: String? = null
)

data class FirestoreTrip(
    val id: String = "",
    val title: String = "",
    val currency: String = "INR",
    val createdBy: String = "",       // uid of creator
    val createdByName: String = "",
    val participants: List<FirestoreParticipant> = emptyList()
)

data class FirestoreCategory(
    val id: String = "",
    val name: String = "",
    val iconName: String = ""
)

// Status values: "pending", "accepted", "declined"
data class FirestoreInvite(
    val id: String = "",
    val tripId: String = "",
    val tripTitle: String = "",
    val fromUid: String = "",
    val fromName: String = "",
    val toUid: String = "",
    val toEmail: String = "",
    val toName: String = "",
    val status: String = "pending",
    val timestamp: Long = System.currentTimeMillis()
)

// Used for searching users
data class FirestoreUser(
    val uid: String = "",
    val name: String = "",
    val email: String = ""
)

//Notifications data class

data class FirestoreNotification(
    val id: String = "",
    val tripId: String = "",
    val tripTitle: String = "",
    val actorUid: String = "",
    val actorName: String = "",
    val recipientUid: String = "",
    val type: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)