package cz.cleevio.repository

import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import timber.log.Timber
import java.util.*

class PhoneNumberUtils constructor(
	val telephonyManager: TelephonyManager
) {
	private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

	//alternative is telephonyManager.simCountryIso
	private val defaultCountryIso = telephonyManager.networkCountryIso
	private val defaultRegion = "+" + defaultCountryIso.uppercase(Locale.getDefault())
	private val defaultCountryCode = when (defaultCountryIso) {
		CZECH_REGION_CODE -> CZECH_PHONE_NUMBER_COUNTRY_CODE_PREFIX
		SLOVAK_REGION_CODE -> SLOVAK_PHONE_NUMBER_COUNTRY_CODE_PREFIX
		else -> ""
	}


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
		val number = toValidPhoneNumber(this)
		return try {
			val phoneNumber = if (number.length == DEFAULT_PHONE_NUMBER_LENGTH) {
				// Country code did not recognized, use default region
				"$defaultCountryCode$number"
			} else if (number.length == SLOVAK_PHONE_NUMBER_LENGTH && number.startsWith("0")) {
				// Slovak phone number -> need to remove "0" from start
				"$defaultCountryCode${number.drop(1)}"
			} else {
				this
			}
			phoneUtil.parse(phoneNumber, defaultRegion)
		} catch (e: NumberParseException) {
			Timber.e("NumberParseException was thrown: $e")
			null
		}
	}

	fun toValidPhoneNumber(phoneNumber: String): String {
		return phoneNumber
			.replace("\\s".toRegex(), "")
			.replace("-".toRegex(), "")
	}

	companion object {
		const val DEFAULT_PHONE_NUMBER_LENGTH = 9
		const val SLOVAK_PHONE_NUMBER_LENGTH = 10

		const val CZECH_REGION_CODE = "cz"
		const val SLOVAK_REGION_CODE = "sk"

		const val CZECH_PHONE_NUMBER_COUNTRY_CODE_PREFIX = "+420"
		const val SLOVAK_PHONE_NUMBER_COUNTRY_CODE_PREFIX = "+421"
	}
}