package com.locket.backend.domain.contact

import com.google.firebase.database.FirebaseDatabase
import com.locket.backend.domain.friend.FriendModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ContactRepository(
    private val contactProvider: ContactProvider,
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    suspend fun getFriendSuggestions(
        myPhoneNumber: String
    ): List<FriendModel> = withContext(Dispatchers.IO) {
        if (!contactProvider.hasPermission()) return@withContext emptyList()

        val deviceContacts = contactProvider.getDeviceContacts()
        if (deviceContacts.isEmpty()) return@withContext emptyList()

        val suggestions = mutableListOf<FriendModel>()
        
        deviceContacts
            .filter { it.normalizedPhoneNumber != myPhoneNumber }
            .distinctBy { it.normalizedPhoneNumber }
            .forEach { contact ->
                suggestions.add(
                    FriendModel(
                        phoneNumber = contact.normalizedPhoneNumber,
                        displayName = contact.Name,
                        relationStatus = "NONE"
                    )
                )
            }

        return@withContext suggestions
    }

    suspend fun checkUserExistsOnFirebase(phone: String): Boolean {
        return try {
            val snapshot = firebaseDatabase.getReference("users").child(phone).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    fun hasContactPermission(): Boolean = contactProvider.hasPermission()
}