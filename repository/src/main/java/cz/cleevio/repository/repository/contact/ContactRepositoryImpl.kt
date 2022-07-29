package cz.cleevio.repository.repository.contact

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Patterns
import com.cleevio.vexl.cryptography.HMAC_PASSWORD
import com.cleevio.vexl.cryptography.HmacCryptoLib
import cz.cleevio.cache.dao.ContactDao
import cz.cleevio.cache.dao.ContactKeyDao
import cz.cleevio.cache.dao.NotificationDao
import cz.cleevio.cache.entity.ContactEntity
import cz.cleevio.cache.entity.ContactKeyEntity
import cz.cleevio.cache.entity.ContactLevel
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.api.ContactApi
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.contact.CommonFriendsRequest
import cz.cleevio.network.request.contact.ContactRequest
import cz.cleevio.network.request.contact.CreateUserRequest
import cz.cleevio.network.request.contact.DeleteContactRequest
import cz.cleevio.network.response.contact.ContactLevelApi
import cz.cleevio.repository.PhoneNumberUtils
import cz.cleevio.repository.R
import cz.cleevio.repository.model.contact.*
import timber.log.Timber

class ContactRepositoryImpl constructor(
	private val contactDao: ContactDao,
	private val contactKeyDao: ContactKeyDao,
	private val contactApi: ContactApi,
	private val phoneNumberUtils: PhoneNumberUtils,
	private val encryptedPreference: EncryptedPreferenceRepository,
	private val notificationDao: NotificationDao,
) : ContactRepository {

	override fun getPhoneContacts(): List<Contact> = contactDao
		.getAllPhoneContacts().map { it.phoneContactFromDao() }

	override fun getFacebookContacts(): List<FacebookContact> = contactDao
		.getAllFacebookContacts().map { it.fbContactFromDao() }

	@Suppress("NestedBlockDepth")
	override suspend fun syncContacts(contentResolver: ContentResolver): Resource<List<Contact>> {
		Timber.tag("ContactSync").d("Starting contact synchronization")
		val contactList: ArrayList<Contact> = ArrayList()

		val projection = arrayOf(
			ContactsContract.Contacts._ID,
			ContactsContract.Data.CONTACT_ID,
			ContactsContract.Contacts.DISPLAY_NAME,
			ContactsContract.Contacts.PHOTO_URI,
			ContactsContract.Contacts.HAS_PHONE_NUMBER,
			ContactsContract.Data.DATA1,
			ContactsContract.Data.MIMETYPE
		)

		val selection = ContactsContract.Data.MIMETYPE + " = ?" + " OR " +
			ContactsContract.Data.MIMETYPE + " = ?" + " OR " +
			ContactsContract.Data.MIMETYPE + " = ?"

		val selectionArgs = arrayOf(
			"%" + "@" + "%",
			ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
			ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
		)

		val order: String = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY

		val cursor = contentResolver.query(
			ContactsContract.Data.CONTENT_URI,
			projection,
			selection,
			selectionArgs,
			order
		)

		Timber.tag("ContactSync").d("Finished querying with count ${cursor?.count}")

		if (cursor != null && cursor.count > 0) {
			while (cursor.moveToNext()) {
				val id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
				val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
				val photo = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))
				val emailOrMobile = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1))
				val hasPhone = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

				var phoneNumber = ""
				var email = ""
				if (hasPhone > 0 && !emailOrMobile.isNullOrEmpty()) {
					if (!isEmailValid(emailOrMobile)) {
						phoneNumber = emailOrMobile
					} else {
						email = emailOrMobile
					}
				}

				val contact = Contact(
					name = name,
					id = id,
					photoUri = if (photo == null) {
						null
					} else {
						Uri.parse(photo)
					},
					phoneNumber = phoneNumber,
					email = email
				)

				if (contact.phoneOrEmail().isNotEmpty()) {
					contactList.add(contact)
				}
			}
		}
		cursor?.close()

		Timber.tag("ContactSync").d("Contacts moved from cursor to list")
		val result = contactList
			.distinctBy { listOf(it.name, it.email, it.phoneNumber.toValidPhoneNumber()) }
			.apply {
				Timber.tag("ContactSync").d("Replacing only ${this.size} after distinctBy")
			}
			//take only valid phone numbers
			.filter { phoneNumberUtils.isPhoneValid(it.phoneNumber) }
			.apply {
				Timber.tag("ContactSync").d("Replacing only ${this.size} after filter")
			}.map {
				//format phone number to proper format
				it.copy(
					phoneNumber = phoneNumberUtils.getFormattedPhoneNumber(it.phoneNumber)
				)
			}

		return Resource.success(data = result)
	}

	private suspend fun saveContactsToDB(contactList: List<Contact>) {
		Timber.tag("ContactSync").d("Replacing ${contactList.size} contacts in the database")
		contactDao.replaceAll(
			contactList.map {
				it.toDao()
			}
		)
		Timber.tag("ContactSync").d("Replacing ${contactList.size} contacts in the database DONE")
	}

	private suspend fun deleteContactsFromDB(contactList: List<Contact>) {
		Timber.tag("DeleteContacts").d("Replacing ${contactList.size} contacts in the database")
		contactList.forEach { contact ->
			contactDao.delete(
				contact.toDao()
			)
		}
		Timber.tag("DeleteContacts").d("Replacing ${contactList.size} contacts in the database DONE")
	}

	override suspend fun checkAllContacts(hashedPhoneNumbers: List<String>) = tryOnline(
		request = { contactApi.postContactNotImported(ContactRequest(hashedPhoneNumbers)) },
		mapper = { it?.newContacts.orEmpty() }
	)

	override suspend fun uploadAllMissingContacts(contacts: List<Contact>): Resource<ContactImport> {
		Timber.tag("ContactSync").d("Starting hashing ${contacts.size} contacts before uploading")
		// hash phone numbers if we are here from onboarding.
		// that means that we have skipped hashing before `not-imported` EP
		val hashedContacts = contacts.map {
			if (it.hashedPhoneNumber.isBlank()) {
				it.copy(hashedPhoneNumber = HmacCryptoLib.digest(HMAC_PASSWORD, it.phoneNumber))
			} else {
				it
			}
		}
		//take those hashed phone numbers as identifiers
		val identifiers = hashedContacts.map {
			it.hashedPhoneNumber
		}
		Timber.tag("ContactSync").d("Hashing is DONE")
		return tryOnline(
			//send identifiers to BE
			request = { contactApi.postContactImport(contactImportRequest = ContactRequest(identifiers)) },
			mapper = { it?.fromNetwork() },
			doOnSuccess = {
				//save contacts to DB including the hashed number
				saveContactsToDB(hashedContacts)
			}
		)
	}

	override suspend fun deleteContacts(contacts: List<Contact>): Resource<Unit> {
		Timber.tag("DeleteContacts").d("Starting hashing ${contacts.size} contacts before uploading")
		// hash phone numbers if we are here from onboarding.
		// that means that we have skipped hashing before `not-imported` EP
		val hashedContacts = contacts.map {
			if (it.hashedPhoneNumber.isBlank()) {
				it.copy(hashedPhoneNumber = HmacCryptoLib.digest(HMAC_PASSWORD, it.phoneNumber))
			} else {
				it
			}
		}

		//take those hashed phone numbers as identifiers
		val identifiers = hashedContacts.map {
			it.hashedPhoneNumber
		}
		Timber.tag("DeleteContacts").d("Hashing is DONE")
		return tryOnline(
			//send identifiers to BE
			request =
			{ contactApi.deleteContact(deleteContactRequest = DeleteContactRequest(identifiers)) },
			mapper = { },
			doOnSuccess =
			{
				//delete contacts from DB including the hashed number
				deleteContactsFromDB(hashedContacts)
			}
		)
	}

	override suspend fun uploadAllMissingFBContacts(identifiers: List<String>): Resource<ContactImport> = tryOnline(
		request = {
			contactApi.postContactImport(
				hash = encryptedPreference.facebookHash,
				signature = encryptedPreference.facebookSignature,
				contactImportRequest = ContactRequest(identifiers)
			)
		},
		mapper = { it?.fromNetwork() }
	)

	override suspend fun syncFacebookContacts(facebookId: String, accessToken: String): Resource<List<FacebookContact>> {
		val response = tryOnline(
			mapper = {
				it?.facebookUser?.friends?.map { friend ->
					friend.fromFacebook()
				}
//				ContactFacebookResponse(
//					facebookUser = FacebookUserResponse(
//						id = "MyFBID",
//						 name = "My name",
//						 picture = Picture(
//							 data = PictureData(
//								 height = 0,
//								 width = 0,
//								 isSilhouette = false,
//									url = "https://i.imgflip.com/1a9v1k.jpg"
//							 )
//						 ),
//						 friends = listOf(
//							 FacebookUserResponse(
//								 id = "SomeFBID1",
//								 name = "Samuel L. Jackson",
//								 picture = Picture(
//									 data = PictureData(
//										 height = 0,
//										 width = 0,
//										 isSilhouette = false,
//										 url = "https://i.imgflip.com/1a9v1k.jpg"
//									 )
//								 ),
//								 friends = emptyList()
//							 ),
//							 FacebookUserResponse(
//								 id = "SomeFBID2",
//								 name = "Spongebob Squarepants",
//								 picture = Picture(
//									 data = PictureData(
//										 height = 0,
//										 width = 0,
//										 isSilhouette = false,
//										 url = "https://www.kidscompany.cz/user/categories/orig/logo-spongebob.png"
//									 )
//								 ),
//								 friends = emptyList()
//							 ),
//							 FacebookUserResponse(
//								 id = "SomeFBID3",
//								 name = "Patrick Star",
//								 picture = Picture(
//									 data = PictureData(
//										 height = 0,
//										 width = 0,
//										 isSilhouette = false,
//										 url = "https://upload.wikimedia.org/wikipedia/en/thumb/3/33/Patrick_Star.svg/1200px-Patrick_Star.svg.png"
//									 )
//								 ),
//								 friends = emptyList()
//							 )
//						 )
//					 )
//				).facebookUser.friends.map { friend ->
//					friend.fromFacebook()
//				}
			},
			request = {
				contactApi.getFacebookUser(
					hash = encryptedPreference.facebookHash,
					signature = encryptedPreference.facebookSignature,
					facebookId = facebookId,
					accessToken = accessToken
				)
			}
		)

		if (response.status != Status.Success) {
			return response
		}

		// TODO replace all?
		contactDao.replaceAll(
			response.data.orEmpty().map { fbContact ->
				ContactEntity(
					contactType = fbContact.getContactType().name,
					name = fbContact.name,
					facebookId = fbContact.facebookId,
					facebookIdHashed = HmacCryptoLib.digest(HMAC_PASSWORD, fbContact.facebookId),
					photoUri = fbContact.photoUri?.toString()
				)
			}
		)

		return Resource.success(getFacebookContacts())
	}

	override suspend fun registerUser(): Resource<Unit> {
		return notificationDao.getOne()?.let { notificationDataStorage ->
			tryOnline(
				request = {
					contactApi.postUsers(
						createUserRequest = CreateUserRequest(
							firebaseToken = notificationDataStorage.token
						)
					)
				},
				mapper = { }
			)
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_firebase_token))
	}

	override suspend fun registerFacebookUser(): Resource<Unit> {
		return notificationDao.getOne()?.let { notificationDataStorage ->
			tryOnline(
				request = {
					contactApi.postUsers(
						hash = encryptedPreference.facebookHash,
						signature = encryptedPreference.facebookSignature,
						createUserRequest = CreateUserRequest(
							firebaseToken = notificationDataStorage.token
						)
					)
				},
				mapper = { }
			)
		} ?: Resource.error(ErrorIdentification.MessageError(message = R.string.error_missing_firebase_token))
	}

	override suspend fun syncMyContactsKeys(): Boolean {
		val first = loadMyContactsKeys(0, Int.MAX_VALUE, ContactLevelApi.FIRST)
		val second = loadMyContactsKeys(0, Int.MAX_VALUE, ContactLevelApi.SECOND)

		val success = first.status == Status.Success && second.status == Status.Success

		if (success) {
			val contactKeys = first.data.orEmpty().map {
				ContactKeyEntity(
					publicKey = it,
					contactLevel = ContactLevel.FIRST
				)
			} + second.data.orEmpty().map {
				ContactKeyEntity(
					publicKey = it,
					contactLevel = ContactLevel.SECOND
				)
			}
			contactKeyDao.replaceAll(contactKeys)
		}

		return success
	}

	override fun getContactKeys(): List<ContactKey> {
		return contactKeyDao.getAllKeys().map {
			it.fromCache()
		}
	}

	override fun getFirstLevelContactKeys(): List<ContactKey> {
		return contactKeyDao.getFirstLevelKeys().map {
			it.fromCache()
		}
	}

	override fun getSecondLevelContactKeys(): List<ContactKey> {
		return contactKeyDao.getSecondLevelKeys().map {
			it.fromCache()
		}
	}

	override fun getAllGroupsContactKeys(): List<ContactKey> {
		return contactKeyDao.getGroupLevelKeys().map {
			it.fromCache()
		}
	}

	override fun getGroupsContactKeys(groupUuids: List<String>): List<ContactKey> {
		val listOfList = groupUuids.map {
			contactKeyDao.getKeysByGroup(it).map { entity ->
				entity.fromCache()
			}
		}
		return listOfList.flatten()
	}

	private suspend fun loadMyContactsKeys(
		page: Int,
		limit: Int,
		levelApi: ContactLevelApi
	): Resource<List<String>> = tryOnline(
		request = { contactApi.getContactsMe(page, limit, levelApi) },
		mapper = { it?.items?.map { item -> item.publicKey }.orEmpty() }
	)

	override suspend fun deleteMyUser(): Resource<Unit> = tryOnline(
		request = { contactApi.deleteUserMe() },
		mapper = { }
	)

	// TODO update when FB user will be available
	override suspend fun deleteMyFacebookUser(): Resource<Unit> = tryOnline(
		request = {
			contactApi.deleteUserMe(
				hash = encryptedPreference.facebookHash,
				signature = encryptedPreference.facebookSignature
			)
		},
		mapper = { }
	)

	override suspend fun generateContactsTmp(): Resource<Unit> = tryOnline(
		request = {
			contactApi.generateContactsTmp(
				hash = encryptedPreference.hash,
				signature = encryptedPreference.signature
			)
		},
		mapper = { }
	)

	override suspend fun generateFacebookContactsTmp(): Resource<Unit> = tryOnline(
		request = {
			contactApi.generateContactsTmp(
				hash = encryptedPreference.facebookHash,
				signature = encryptedPreference.facebookSignature
			)
		},
		mapper = { }
	)

	override suspend fun getCommonFriends(contactsPublicKeys: Collection<String>): Map<String, List<BaseContact>> {
		val facebookContacts = contactApi.getCommonContacts(
			hash = encryptedPreference.facebookHash,
			signature = encryptedPreference.facebookSignature,
			commonFriendsRequest = CommonFriendsRequest(
				publicKeys = contactsPublicKeys
			)
		)

		val phoneContacts = contactApi.getCommonContacts(
			hash = encryptedPreference.hash,
			signature = encryptedPreference.signature,
			commonFriendsRequest = CommonFriendsRequest(
				publicKeys = contactsPublicKeys
			)
		)

		val commonFriendsMap = mutableMapOf<String, List<BaseContact>>()

		if (phoneContacts.isSuccessful) {
			phoneContacts.body()?.commonContacts.orEmpty().map { friend ->
				val contacts = contactDao.getAllPhoneContacts()
					.map { it.fromDao() }
					.filter { friend.common.hashes.contains(it.getHashedContact()) }
				commonFriendsMap.put(friend.publicKey, contacts)
			}
		}

		if (facebookContacts.isSuccessful) {
			facebookContacts.body()?.commonContacts.orEmpty().map { friend ->
				val contacts = contactDao.getAllFacebookContacts()
					.map { it.fromDao() }
					.filter { friend.common.hashes.contains(it.getHashedContact()) }
				if (commonFriendsMap.containsKey(friend.publicKey)) {
					commonFriendsMap[friend.publicKey] = commonFriendsMap[friend.publicKey].orEmpty() + contacts
				} else {
					commonFriendsMap.put(friend.publicKey, contacts)
				}
			}
		}

		return commonFriendsMap
	}

	private fun isEmailValid(email: String): Boolean =
		!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()

	private fun String.toValidPhoneNumber(): String {
		return this
			.replace("\\s".toRegex(), "")
			.replace("-".toRegex(), "")
	}
}