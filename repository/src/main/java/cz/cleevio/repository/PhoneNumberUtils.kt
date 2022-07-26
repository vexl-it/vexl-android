package cz.cleevio.repository

import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import timber.log.Timber
import java.util.*

class PhoneNumberUtils constructor(
	telephonyManager: TelephonyManager
) {
	private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

	//alternative is telephonyManager.simCountryIso
	private val defaultRegion = "+" + telephonyManager.networkCountryIso.uppercase(Locale.getDefault())

	fun isPhoneValid(phoneNumber: String): Boolean {
		return phoneNumber.parsePhoneNumber(phoneUtil)?.let {
			phoneUtil.isValidNumber(it)
		} ?: false
	}

	fun getFormattedPhoneNumber(phoneNumber: String): String {
		val parsedPhoneNumber = phoneNumber.parsePhoneNumber(phoneUtil)
		return "+" + parsedPhoneNumber?.countryCode.toString() + parsedPhoneNumber?.nationalNumber.toString()
	}

	private fun String.parsePhoneNumber(phoneUtil: PhoneNumberUtil): Phonenumber.PhoneNumber? {
		return try {
			phoneUtil.parse(this, defaultRegion)
		} catch (e: NumberParseException) {
			Timber.e("NumberParseException was thrown: $e")
			null
		}
	}
}