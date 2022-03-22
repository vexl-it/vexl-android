package cz.cleevio.repository.repository.contact

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Patterns
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import cz.cleevio.cache.dao.ContactDao
import cz.cleevio.cache.entity.ContactEntity
import cz.cleevio.network.api.ContactApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.contact.ContactRequest
import cz.cleevio.repository.model.contact.Contact
import cz.cleevio.repository.model.contact.ContactImport
import cz.cleevio.repository.model.contact.fromDao
import cz.cleevio.repository.model.contact.fromNetwork
import timber.log.Timber
import java.util.*

class ContactRepositoryImpl constructor(
	private val contactDao: ContactDao,
	private val contactApi: ContactApi,
	private val telephonyManager: TelephonyManager
) : ContactRepository {

	override fun getContacts(): List<Contact> = contactDao
		.getAllContacts().map { it.fromDao() }

	override suspend fun syncContacts(contentResolver: ContentResolver): Resource<Unit> {
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

		if (cursor != null && cursor.count > 0) {
			while (cursor.moveToNext()) {
				val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
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

		val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
		contactDao.replaceAll(contactList
			.distinctBy { listOf(it.name, it.email, it.phoneNumber.toValidPhoneNumber()) }
			.filter { it.phoneNumber.parsePhoneNumber(phoneUtil) != null && phoneUtil.isValidNumber(it.phoneNumber.parsePhoneNumber(phoneUtil)) }
			.map {
				val phoneNumber = it.phoneNumber.parsePhoneNumber(phoneUtil)
				ContactEntity(
					id = it.id.toLong(),
					name = it.name,
					phone = phoneNumber?.countryCode.toString() + phoneNumber?.nationalNumber.toString(),  //.it.phoneNumber,
					email = it.email,
					photoUri = it.photoUri.toString()
				)
			})

		return Resource.success(data = Unit)
	}

	override suspend fun checkAllContacts(phoneNumbers: List<String>) = tryOnline(
		request = { contactApi.postContactNotImported(ContactRequest(phoneNumbers)) },
		mapper = { it?.newContacts.orEmpty() }
	)

	override suspend fun uploadAllMissingContacts(phoneNumbers: List<String>): Resource<ContactImport> = tryOnline(
		request = { contactApi.postContactImport(ContactRequest(phoneNumbers)) },
		mapper = { it?.fromNetwork() }
	)

	private fun isEmailValid(email: String): Boolean =
		!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()

	fun String.toValidPhoneNumber(): String {
		return this
			.replace("\\s".toRegex(), "")
			.replace("-".toRegex(), "")
	}

	fun String.parsePhoneNumber(phoneUtil: PhoneNumberUtil): Phonenumber.PhoneNumber? {
		return try {
			//phoneUtil.parse(this, telephonyManager.simCountryIso.uppercase(Locale.getDefault())) 	//alternative is telephonyManager.networkCountryIso
			phoneUtil.parse(this, telephonyManager.networkCountryIso.uppercase(Locale.getDefault()))    //alternative is telephonyManager.simCountryIso
		} catch (e: NumberParseException) {
			Timber.e("NumberParseException was thrown: $e")
			null
		}
	}

	fun String.isPhoneValid(): Boolean =
		this.matches("^\\+(?:[0-9] ?){6,14}[0-9]\$".toRegex())
}