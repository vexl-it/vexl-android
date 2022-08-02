package cz.cleevio.onboarding.ui.initPhoneFragment

import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.KeyPairCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.user.ConfirmPhone
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class InitPhoneViewModel constructor(
	private val encryptedPreferences: EncryptedPreferenceRepository,
	private val userRepository: UserRepository
) : BaseViewModel() {

	private val _phoneNumberSuccess = Channel<InitPhoneSuccess>(Channel.CONFLATED)
	val phoneNumberSuccess = _phoneNumberSuccess.receiveAsFlow()

	init {
		loadKeys()
	}

	fun sendPhoneNumber(phoneNumber: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = userRepository.authStepOne(phoneNumber)
			Timber.tag("ASDX").d("${response.data}")

			when (response.status) {
				is Status.Success -> response.data?.let { confirmPhone ->
					_phoneNumberSuccess.send(
						InitPhoneSuccess(
							phoneNumber = phoneNumber,
							confirmPhone = confirmPhone
						)
					)
				}
			}
		}
	}

	fun loadKeys() {
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
