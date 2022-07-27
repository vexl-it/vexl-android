package cz.cleevio.vexl.lightbase.core.extensions

import android.app.Activity
import android.content.*
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.i18n.phonenumbers.PhoneNumberUtil
import timber.log.Timber

fun String.formatPhoneNumber(code: String): String =
	PhoneNumberUtils.formatNumber(this, code) ?: this

fun Fragment.createRegisterSmsVerifBR(
	onReceiveSmsCode: (String) -> Unit
): BroadcastReceiver {
	val smsVerificationBottomSheetResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == Activity.RESULT_OK && result.data != null) {
				val code = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE).orEmpty()
				onReceiveSmsCode(code)
			}
		}
	val smsVerificationBroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
				val status = intent.extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
				if (status?.statusCode == CommonStatusCodes.SUCCESS) {
					val consentIntent: Intent? = intent.extras?.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT)
					try {
						smsVerificationBottomSheetResultLauncher.launch(consentIntent)
					} catch (e: ActivityNotFoundException) {
						Timber.e(e)
					}
				}
			}
		}
	}

	registerSmsVerificationBR(smsVerificationBroadcastReceiver)
	SmsRetriever.getClient(requireContext()).startSmsUserConsent(null)

	return smsVerificationBroadcastReceiver
}

fun Fragment.registerSmsVerificationBR(receiver: BroadcastReceiver) {
	requireActivity().registerReceiver(
		receiver,
		IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
		SmsRetriever.SEND_PERMISSION,
		null
	)
}

fun EditText.phoneNumberTextWatcher(
	countryCode: String,
	onTextChanged: (String) -> Unit
): TextWatcher {
	val asYouTypeFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(countryCode)
	val textWatcher = object : TextWatcher {
		@Synchronized
		override fun afterTextChanged(editable: Editable?) {
			onTextChanged(editable?.toString().orEmpty())
			removeTextChangedListener(this)
			asYouTypeFormatter.clear()
			var formattedPhoneNumber = ""
			editable?.forEach {
				if (PhoneNumberUtils.isNonSeparator(it)) {
					formattedPhoneNumber = asYouTypeFormatter.inputDigit(it)
				}
			}
			editable?.replace(
				0,
				editable.length,
				formattedPhoneNumber,
				0,
				formattedPhoneNumber.length
			)
			addTextChangedListener(this)
		}

		override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

		override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
	}

	this.addTextChangedListener(textWatcher)
	return textWatcher
}
