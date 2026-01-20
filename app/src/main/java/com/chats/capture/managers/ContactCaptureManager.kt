package com.chats.capture.managers

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.chats.capture.CaptureApplication
import com.chats.capture.database.ContactDao
import com.chats.capture.models.Contact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages contact capture from device contacts
 */
class ContactCaptureManager(private val context: Context) {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val contactDao: ContactDao = (context.applicationContext as CaptureApplication).database.contactDao()
    
    /**
     * Capture all contacts from device
     */
    fun captureAllContacts() {
        serviceScope.launch {
            try {
                Timber.d("Starting contact capture")
                val contacts = readAllContacts()
                
                if (contacts.isEmpty()) {
                    Timber.w("No contacts found on device")
                    return@launch
                }
                
                // Insert or update contacts
                contacts.forEach { contact ->
                    // Check if contact already exists (by phone or email)
                    val existing = contactDao.findContact(contact.phoneNumber, contact.email)
                    
                    if (existing != null) {
                        // Update existing contact if data changed
                        if (hasContactChanged(existing, contact)) {
                            val updated = existing.copy(
                                name = contact.name,
                                phoneNumber = contact.phoneNumber,
                                email = contact.email,
                                organization = contact.organization,
                                jobTitle = contact.jobTitle,
                                address = contact.address,
                                notes = contact.notes,
                                photoUri = contact.photoUri,
                                timestamp = System.currentTimeMillis(),
                                synced = false // Mark as unsynced if updated
                            )
                            contactDao.updateContact(updated)
                            Timber.d("Updated contact: ${contact.name}")
                        }
                    } else {
                        // Insert new contact
                        contactDao.insertContact(contact)
                        Timber.d("Captured new contact: ${contact.name}")
                    }
                }
                
                Timber.d("Contact capture completed: ${contacts.size} contacts processed")
            } catch (e: SecurityException) {
                Timber.e(e, "Permission denied: READ_CONTACTS permission required")
            } catch (e: Exception) {
                Timber.e(e, "Error capturing contacts")
            }
        }
    }
    
    /**
     * Read all contacts from device
     */
    private suspend fun readAllContacts(): List<Contact> {
        // Check permission before accessing contacts
        if (android.content.pm.PackageManager.PERMISSION_GRANTED != 
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)) {
            Timber.w("READ_CONTACTS permission not granted")
            throw SecurityException("READ_CONTACTS permission required")
        }
        
        val contacts = mutableListOf<Contact>()
        val contentResolver: ContentResolver = context.contentResolver
        
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
            ContactsContract.Contacts.PHOTO_URI
        )
        
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )
        
        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
            val photoIndex = it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
            
            while (it.moveToNext()) {
                val contactId = it.getString(idIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val hasPhone = it.getInt(hasPhoneIndex) > 0
                val photoUri = it.getString(photoIndex)
                
                // Get phone numbers
                val phoneNumbers = if (hasPhone) {
                    getPhoneNumbers(contentResolver, contactId)
                } else {
                    emptyList()
                }
                
                // Get email addresses
                val emails = getEmails(contentResolver, contactId)
                
                // Get organization info
                val (organization, jobTitle) = getOrganizationInfo(contentResolver, contactId)
                
                // Get address
                val address = getAddress(contentResolver, contactId)
                
                // Get notes
                val notes = getNotes(contentResolver, contactId)
                
                val deviceId = DeviceRegistrationManager(context).getDeviceId()
                
                // Create contact entry for each phone number or email
                if (phoneNumbers.isNotEmpty()) {
                    phoneNumbers.forEach { phone ->
                        contacts.add(
                            Contact(
                                deviceId = deviceId,
                                name = name,
                                phoneNumber = phone,
                                email = emails.firstOrNull(),
                                organization = organization,
                                jobTitle = jobTitle,
                                address = address,
                                notes = notes,
                                photoUri = photoUri,
                                timestamp = System.currentTimeMillis(),
                                synced = false
                            )
                        )
                    }
                } else if (emails.isNotEmpty()) {
                    // If no phone, create entry for each email
                    emails.forEach { email ->
                        contacts.add(
                            Contact(
                                deviceId = deviceId,
                                name = name,
                                phoneNumber = null,
                                email = email,
                                organization = organization,
                                jobTitle = jobTitle,
                                address = address,
                                notes = notes,
                                photoUri = photoUri,
                                timestamp = System.currentTimeMillis(),
                                synced = false
                            )
                        )
                    }
                } else {
                    // Contact with no phone or email
                    contacts.add(
                        Contact(
                            deviceId = deviceId,
                            name = name,
                            phoneNumber = null,
                            email = null,
                            organization = organization,
                            jobTitle = jobTitle,
                            address = address,
                            notes = notes,
                            photoUri = photoUri,
                            timestamp = System.currentTimeMillis(),
                            synced = false
                        )
                    )
                }
            }
        }
        
        return contacts
    }
    
    private fun getPhoneNumbers(contentResolver: ContentResolver, contactId: String): List<String> {
        val phones = mutableListOf<String>()
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )
        
        phoneCursor?.use {
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val phone = it.getString(numberIndex)
                if (phone != null && phone.isNotBlank()) {
                    phones.add(phone)
                }
            }
        }
        
        return phones
    }
    
    private fun getEmails(contentResolver: ContentResolver, contactId: String): List<String> {
        val emails = mutableListOf<String>()
        val emailCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.DATA),
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )
        
        emailCursor?.use {
            val emailIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
            while (it.moveToNext()) {
                val email = it.getString(emailIndex)
                if (email != null && email.isNotBlank()) {
                    emails.add(email)
                }
            }
        }
        
        return emails
    }
    
    private fun getOrganizationInfo(contentResolver: ContentResolver, contactId: String): Pair<String?, String?> {
        var organization: String? = null
        var jobTitle: String? = null
        
        val orgCursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Organization.COMPANY,
                ContactsContract.CommonDataKinds.Organization.TITLE
            ),
            ContactsContract.Data.CONTACT_ID + " = ? AND " +
                    ContactsContract.Data.MIMETYPE + " = ?",
            arrayOf(
                contactId,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
            ),
            null
        )
        
        orgCursor?.use {
            val companyIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)
            val titleIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE)
            
            if (it.moveToFirst()) {
                organization = it.getString(companyIndex)
                jobTitle = it.getString(titleIndex)
            }
        }
        
        return Pair(organization, jobTitle)
    }
    
    private fun getAddress(contentResolver: ContentResolver, contactId: String): String? {
        val addressCursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS),
            ContactsContract.Data.CONTACT_ID + " = ? AND " +
                    ContactsContract.Data.MIMETYPE + " = ?",
            arrayOf(
                contactId,
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
            ),
            null
        )
        
        addressCursor?.use {
            val addressIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
            if (it.moveToFirst()) {
                return it.getString(addressIndex)
            }
        }
        
        return null
    }
    
    private fun getNotes(contentResolver: ContentResolver, contactId: String): String? {
        val notesCursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Note.NOTE),
            ContactsContract.Data.CONTACT_ID + " = ? AND " +
                    ContactsContract.Data.MIMETYPE + " = ?",
            arrayOf(
                contactId,
                ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
            ),
            null
        )
        
        notesCursor?.use {
            val noteIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)
            if (it.moveToFirst()) {
                return it.getString(noteIndex)
            }
        }
        
        return null
    }
    
    private fun hasContactChanged(existing: Contact, new: Contact): Boolean {
        return existing.name != new.name ||
                existing.phoneNumber != new.phoneNumber ||
                existing.email != new.email ||
                existing.organization != new.organization ||
                existing.jobTitle != new.jobTitle ||
                existing.address != new.address ||
                existing.notes != new.notes
    }
}
