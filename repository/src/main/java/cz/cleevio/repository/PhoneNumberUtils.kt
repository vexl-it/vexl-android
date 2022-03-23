package cz.cleevio.repository

import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import timber.log.Timber
import java.util.*


class PhoneNumberUtils constructor(
	private val telephonyManager: TelephonyManager
) {

	private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

	fun isPhoneValid(phoneNumber: String): Boolean {
		return phoneNumber.parsePhoneNumber(phoneUtil) != null &&
			phoneUtil.isValidNumber(phoneNumber.parsePhoneNumber(phoneUtil))
	}

	fun getFormattedPhoneNumber(phoneNumber: String): String {
		val parsedPhoneNumber = phoneNumber.parsePhoneNumber(phoneUtil)
		return parsedPhoneNumber?.countryCode.toString() + parsedPhoneNumber?.nationalNumber.toString()
	}

	private fun String.parsePhoneNumber(phoneUtil: PhoneNumberUtil): Phonenumber.PhoneNumber? {
		return try {
			//alternative is telephonyManager.networkCountryIso
			//phoneUtil.parse(this, telephonyManager.simCountryIso.uppercase(Locale.getDefault()))
			phoneUtil.parse(this, telephonyManager.networkCountryIso.uppercase(Locale.getDefault()))
		} catch (e: NumberParseException) {
			Timber.e("NumberParseException was thrown: $e")
			null
		}
	}
}