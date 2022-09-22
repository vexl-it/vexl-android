package cz.cleevio.repository

import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import timber.log.Timber

class PhoneNumberUtils constructor(
	val telephonyManager: TelephonyManager,
	val encryptedPreference: EncryptedPreferenceRepository,
) {
	private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

	var defaultRegion: String? = null

	private fun isRegionSet() {
		if (defaultRegion == null) {
			defaultRegion = phoneUtil.getRegionCodeForCountryCode(
				//removing "+" character
				encryptedPreference.userCountryCode.drop(1).toInt()
			)
		}
	}

	fun isPhoneValid(phoneNumber: String): Boolean {
		isRegionSet()
		return phoneNumber.parsePhoneNumber(phoneUtil)?.let {
			phoneUtil.isValidNumber(it)
		} ?: false
	}

	fun getFormattedPhoneNumber(phoneNumber: String): String {
		val parsedPhoneNumber = phoneNumber.parsePhoneNumber(phoneUtil)
		return "+" + parsedPhoneNumber?.countryCode.toString() + parsedPhoneNumber?.nationalNumber.toString()
	}

	private fun String.parsePhoneNumber(phoneUtil: PhoneNumberUtil): Phonenumber.PhoneNumber? {
		isRegionSet()
		val number = toValidPhoneNumber(this)
		return try {
			phoneUtil.parse(number, defaultRegion)
		} catch (e: NumberParseException) {
			Timber.w("NumberParseException was thrown: $e")
			null
		}
	}

	fun toValidPhoneNumber(phoneNumber: String): String {
		return phoneNumber
			.replace("\\s".toRegex(), "")
			.replace("\\(".toRegex(), "")
			.replace("\\)".toRegex(), "")
			.replace("-".toRegex(), "")
	}
}