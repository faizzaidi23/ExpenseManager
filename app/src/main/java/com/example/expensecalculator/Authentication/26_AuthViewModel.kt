package com.example.expensecalculator.Authentication

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val auth = FirebaseAuth.getInstance()


    var name by mutableStateOf("")
    fun onNameChange(v:String){name=v}

    val isLoggedIn get() = auth.currentUser != null

    fun onEmailChange(v: String) { email = v }
    fun onPasswordChange(v: String) { password = v }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                onSuccess()
            } catch (e: Exception) {
                val msg = "Login failed: ${e.message}"
                errorMessage = msg
                onError(msg)
            } finally {
                isLoading = false
            }
        }
    }
    fun register(name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: return@launch

                // Update Firebase display name
                val profileUpdate = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                result.user?.updateProfile(profileUpdate)?.await()

                // Save to Firestore
                saveUserProfile(uid, name, email)
                onSuccess()
            } catch (e: Exception) {
                onError("Registration failed: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun forgotPassword(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                auth.sendPasswordResetEmail(email).await()
                onSuccess()
            } catch (e: Exception) {
                onError("Failed: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user ?: return@launch

                // Save/update profile for ALL Google sign-ins, not just new users
                // Use better name fallback: email prefix if display name is empty
                val displayName = user.displayName?.takeIf { it.isNotBlank() }
                    ?: user.email?.substringBefore("@")
                    ?: "User"

                saveUserProfile(
                    uid = user.uid,
                    name = displayName,
                    email = user.email ?: ""
                )
                onSuccess()
            } catch (e: Exception) {
                onError("Google sign-in failed: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getGoogleSignInClient(context: Context) =
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1087169199906-elmu6f6qdlut9lnbs5dl22j25h2fifj3.apps.googleusercontent.com") // from google-services.json
                .requestEmail()
                .build()
        )

    private suspend fun saveUserProfile(uid: String, name: String, email: String) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "nameLower" to name.lowercase()
        )
        // Use SetOptions.merge() to preserve existing data and only update these fields
        db.collection("users").document(uid).set(user, com.google.firebase.firestore.SetOptions.merge()).await()
    }



    fun deleteAccount(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                val user = auth.currentUser ?: throw Exception("No user logged in")
                val uid = user.uid
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                // 1. Delete all invites sent by user
                val sentInvites = db.collection("trip_invites").whereEqualTo("fromUid", uid).get().await()
                sentInvites.documents.forEach { it.reference.delete().await() }

                // 2. Delete all invites received by user
                val receivedInvites = db.collection("trip_invites").whereEqualTo("toUid", uid).get().await()
                receivedInvites.documents.forEach { it.reference.delete().await() }

                // 2B. Delete notifications intended for this user
                val notifications = db.collection("trip_notifications").whereEqualTo("recipientUid", uid).get().await()
                notifications.documents.forEach { it.reference.delete().await() }

                // 3. Handle trips they are a participant of
                val participantTrips = db.collection("trips").whereArrayContains("participantUids", uid).get().await()

                participantTrips.documents.forEach { doc ->
                    val createdBy = doc.getString("createdBy")
                    if (createdBy == uid) {
                        // If they created the trip, delete the whole trip entirely
                        doc.reference.delete().await()
                    } else {
                        val anonymizedName = "Deleted User"

                        // 3A. Anonymize in the main participants list
                        val participants = doc.get("participants") as? List<Map<String, Any>> ?: emptyList()
                        val updatedParticipants = participants.map { p ->
                            if (p["uid"] == uid) mapOf("uid" to uid, "name" to anonymizedName, "email" to "") else p
                        }
                        doc.reference.update("participants", updatedParticipants).await()

                        // 3B. Cascade the anonymized name to all expenses using UIDs!
                        val expensesSnapshot = doc.reference.collection("expenses").get().await()
                        expensesSnapshot.documents.forEach { expDoc ->
                            var needsUpdate = false
                            val expData = expDoc.data?.toMutableMap() ?: return@forEach

                            if (expData["paidByUid"] == uid) {
                                expData["paidByName"] = anonymizedName
                                needsUpdate = true
                            }

                            val splits = expData["splits"] as? List<Map<String, Any>>
                            if (splits != null) {
                                val updatedSplits = splits.map { split ->
                                    if (split["uid"] == uid) {
                                        needsUpdate = true
                                        split.toMutableMap().apply { this["name"] = anonymizedName }
                                    } else split
                                }
                                if (needsUpdate) expData["splits"] = updatedSplits
                            }
                            if (needsUpdate) expDoc.reference.set(expData).await()
                        }

                        // 3C. Cascade the anonymized name to all paid settlements using UIDs!
                        val settlementsSnapshot = doc.reference.collection("paid_settlements").get().await()
                        settlementsSnapshot.documents.forEach { setDoc ->
                            var needsUpdate = false
                            val setData = setDoc.data?.toMutableMap() ?: return@forEach

                            if (setData["fromUid"] == uid) {
                                setData["fromName"] = anonymizedName
                                needsUpdate = true
                            }
                            if (setData["toUid"] == uid) {
                                setData["toName"] = anonymizedName
                                needsUpdate = true
                            }
                            if (needsUpdate) setDoc.reference.set(setData).await()
                        }
                    }
                }

                // 4. Delete Firestore user document
                db.collection("users").document(uid).delete().await()

                // 5. Delete Firebase Auth account
                user.delete().await()

                onSuccess()
            } catch (e: Exception) {
                if (e is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                    onError("For security, please log out and log back in before deleting your account.")
                } else {
                    onError("Failed to delete account: ${e.message}")
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProfile(
        newName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                val user = auth.currentUser ?: throw Exception("No user logged in")
                val uid = user.uid
                val oldName = user.displayName ?: ""

                // 1. Update Firebase Auth Profile
                val profileUpdate = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                user.updateProfile(profileUpdate).await()

                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                // 2. Update Firestore User Document
                val updates = mapOf(
                    "name" to newName,
                    "nameLower" to newName.lowercase()
                )
                db.collection("users").document(uid)
                    .set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .await()

                // 3. CASCADE THE NAME CHANGE TO ALL TRIPS
                if (oldName.isNotEmpty() && oldName != newName) {
                    val participantTrips = db.collection("trips")
                        .whereArrayContains("participantUids", uid)
                        .get().await()

                    participantTrips.documents.forEach { doc ->
                        // A. Update in the trip's participants list
                        val participants = doc.get("participants") as? List<Map<String, Any>> ?: emptyList()
                        val updatedParticipants = participants.map { p ->
                            if (p["uid"] == uid) {
                                p.toMutableMap().apply { this["name"] = newName }
                            } else p
                        }
                        doc.reference.update("participants", updatedParticipants).await()

                        // B. Update all expenses
                        val expensesSnapshot = doc.reference.collection("expenses").get().await()
                        expensesSnapshot.documents.forEach { expDoc ->
                            var needsUpdate = false
                            val expData = expDoc.data?.toMutableMap() ?: return@forEach

                            // Check if they paid
                            if (expData["paidByName"] == oldName) {
                                expData["paidByName"] = newName
                                needsUpdate = true
                            }

                            // Check if they were in the split
                            val splits = expData["splits"] as? List<Map<String, Any>>
                            if (splits != null) {
                                val updatedSplits = splits.map { split ->
                                    if (split["name"] == oldName) {
                                        needsUpdate = true
                                        split.toMutableMap().apply { this["name"] = newName }
                                    } else {
                                        split
                                    }
                                }
                                if (needsUpdate) {
                                    expData["splits"] = updatedSplits
                                }
                            }

                            if (needsUpdate) expDoc.reference.set(expData).await()
                        }

                        // C. Update all paid settlements
                        val settlementsSnapshot = doc.reference.collection("paid_settlements").get().await()
                        settlementsSnapshot.documents.forEach { setDoc ->
                            var needsUpdate = false
                            val setData = setDoc.data?.toMutableMap() ?: return@forEach

                            if (setData["fromName"] == oldName) {
                                setData["fromName"] = newName
                                needsUpdate = true
                            }
                            if (setData["toName"] == oldName) {
                                setData["toName"] = newName
                                needsUpdate = true
                            }

                            if (needsUpdate) setDoc.reference.set(setData).await()
                        }
                    }
                }

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update profile")
            } finally {
                isLoading = false
            }
        }
    }
}