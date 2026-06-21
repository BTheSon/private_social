package com.locket.backend.domain.contact

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

class ContactProvider(private val context: Context) {

    /**
     * Kiểm tra app đã được cấp quyền đọc danh bạ chưa.
     * Gọi hàm này trước khi gọi getDeviceContacts() để tránh crash.
     */
    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getDeviceContacts(): List<ContactModel> {
        if (!hasPermission()) return emptyList()

        val contacts = mutableListOf<ContactModel>()
        val seenNumbers = mutableSetOf<String>() // 1 người có thể có nhiều số trùng định dạng -> chỉ lấy 1

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)?.takeIf { n -> n.isNotBlank() } ?: continue
                val rawNumber = it.getString(numberIndex) ?: continue
                val normalized = normalizePhoneNumber(rawNumber) ?: continue

                if (seenNumbers.add(normalized)) {
                    contacts.add(
                        ContactModel(
                            Name = name,
                            Phone = rawNumber,
                            normalizedPhoneNumber = normalized
                        )
                    )
                }
            }
        }

        return contacts
    }

    private fun normalizePhoneNumber(input: String): String? {
        val result = com.example.backend.domain.contact.PhoneNumberNormalizer.normalize(input)
        return result.ifBlank { null }
    }
}