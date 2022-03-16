package cz.cleevio.repository.repository.contact

import android.content.ContentResolver
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.contact.Contact
import cz.cleevio.repository.model.contact.ContactImport
import kotlinx.coroutines.flow.Flow

interface ContactRepository {

	suspend fun checkAllContacts(phoneNumbers: List<String>): Resource<List<String>>

	suspend fun uploadAllMissingContacts(phoneNumbers: List<String>): Resource<ContactImport>

	//sync contacts between phone and app DB, also uploads to BE
	suspend fun syncContacts(contentResolver: ContentResolver)

	fun getContactsFlow(): Flow<List<Contact>>

}