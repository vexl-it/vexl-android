package cz.cleevio.repository.repository.contact

import android.content.ContentResolver
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.contact.*

interface ContactRepository {

	suspend fun checkAllContacts(hashedPhoneNumbers: List<String>): Resource<List<String>>

	suspend fun uploadAllMissingContacts(contacts: List<Contact>): Resource<ContactImport>

	suspend fun uploadAllMissingFBContacts(identifiers: List<String>): Resource<ContactImport>

	suspend fun registerUser(): Resource<ContactUser>

	suspend fun registerFacebookUser(): Resource<ContactUser>

	suspend fun syncMyContactsKeys(): Boolean

	fun getContactKeys(): List<ContactKey>

	fun getFirstLevelContactKeys(): List<ContactKey>

	suspend fun deleteMyUser(): Resource<Unit>

	suspend fun deleteMyFacebookUser(): Resource<Unit>

	//sync contacts between phone and app DB, also uploads to BE
	suspend fun syncContacts(contentResolver: ContentResolver): Resource<List<Contact>>

	fun getPhoneContacts(): List<Contact>
	fun getFacebookContacts(): List<FacebookContact>

	suspend fun syncFacebookContacts(facebookId: String, accessToken: String): Resource<List<FacebookContact>>

	suspend fun generateContactsTmp(): Resource<Unit>
	suspend fun generateFacebookContactsTmp(): Resource<Unit>
	suspend fun getCommonFriends(contactsPublicKeys: Set<String>): Map<String, List<BaseContact>>
}