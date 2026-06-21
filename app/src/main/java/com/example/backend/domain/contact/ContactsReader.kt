package com.example.backend.domain.contact

import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsReader(private val context: Context) {
    suspend fun readContacts(): List<DeviceContact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<DeviceContact>()

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            // Dùng Set để loại bỏ số trùng lặp ngay khi đọc
            val seenNumbers = mutableSetOf<String>()

            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: continue
                val rawNumber = it.getString(numberIndex) ?: continue

                val normalized = PhoneNumberNormalizer.normalize(rawNumber)

                // Bỏ qua số không hợp lệ hoặc đã gặp
                if (normalized.isNotBlank() && seenNumbers.add(normalized)) {
                    contacts.add(
                        DeviceContact(
                            name = name,
                            rawPhoneNumber = rawNumber,
                            normalizedPhoneNumber = normalized
                        )
                    )
                }
            }
        }


        contacts
    }
}