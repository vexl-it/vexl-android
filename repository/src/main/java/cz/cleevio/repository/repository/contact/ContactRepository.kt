package cz.cleevio.repository.repository.contact

import android.content.ContentResolver
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.contact.Contact
import cz.cleevio.repository.model.contact.ContactImport
import cz.cleevio.repository.model.contact.ContactUser
import cz.cleevio.repository.model.contact.FacebookContact

interface ContactRepository {

	suspend fun checkAllContacts(phoneNumbers: List<String>): Resource<List<String>>

	suspend fun uploadAllMissingContacts(phoneNumbers: List<String>): Resource<ContactImport>

	suspend fun registerUserWithContactService(): Resource<ContactUser>

	suspend fun loadMyContactsKeys(): Resource<List<String>>

	suspend fun deleteMe(): Resource<Unit>

	//sync contacts between phone and app DB, also uploads to BE
	suspend fun syncContacts(contentResolver: ContentResolver): Resource<Unit>

	fun getContacts(): List<Contact>

	suspend fun getFacebookContacts(facebookId: String, accessToken: String): Resource<List<FacebookContact>>
}