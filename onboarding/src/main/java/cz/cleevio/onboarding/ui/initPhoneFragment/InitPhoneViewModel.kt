package cz.cleevio.onboarding.ui.initPhoneFragment

import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.KeyPairCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.Currency
import cz.cleevio.repository.model.user.ConfirmPhone
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class InitPhoneViewModel constructor(
	private val encryptedPreferences: EncryptedPreferenceRepository,
	private val userRepository: UserRepository,
	val phoneNumberUtil: PhoneNumberUtil
) : BaseViewModel() {

	private val _phoneNumberSuccess = Channel<InitPhoneSuccess>(Channel.CONFLATED)
	val phoneNumberSuccess = _phoneNumberSuccess.receiveAsFlow()

	private val _loading = Channel<Boolean>()
	val loading = _loading.receiveAsFlow()

	init {
		loadKeys()
	}

	fun sendPhoneNumber(countryCode: String, phoneNumber: String, isoCode: String?) {
		viewModelScope.launch(Dispatchers.IO) {
			_loading.send(true)
			val response = userRepository.authStepOne(phoneNumber)
			Timber.tag("ASDX").d("${response.data}")
			_loading.send(false)

			when (response.status) {
				is Status.Success -> response.data?.let { confirmPhone ->
					encryptedPreferences.userCountryCode = countryCode
					encryptedPreferences.userPhoneNumber = phoneNumber
					encryptedPreferences.selectedCurrency = Currency.getCurrencyFromIsoCode(isoCode)
					_phoneNumberSuccess.send(
						InitPhoneSuccess(
							phoneNumber = phoneNumber,
							confirmPhone = confirmPhone
						)
					)
				}
				else -> Unit
			}
		}
	}

	private fun loadKeys() {
		viewModelScope.launch {
			//check if keys already exists
			if (encryptedPreferences.userPrivateKey.isBlank() || encryptedPreferences.userPublicKey.isBlank()) {
				//if not, generate and save to shared preferences
				val keyPair = KeyPairCryptoLib.generateKeyPair()
				saveKeys(keyPair)
			}
		}
	}

	private fun saveKeys(keys: KeyPair) {
		encryptedPreferences.userPrivateKey = keys.privateKey
		encryptedPreferences.userPublicKey = keys.publicKey
	}
}

data class InitPhoneSuccess constructor(
	val phoneNumber: String,
	val confirmPhone: ConfirmPhone
)
