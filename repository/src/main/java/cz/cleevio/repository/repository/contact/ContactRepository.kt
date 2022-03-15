package cz.cleevio.repository.repository.contact

import android.content.ContentResolver
import cz.cleevio.repository.model.contact.Contact
import kotlinx.coroutines.flow.Flow


interface ContactRepository {

	suspend fun syncContacts(contentResolver: ContentResolver)

	fun getContacts(): Flow<List<Contact>>
}