package cz.cleevio.lightspeedskeleton.ui.splashFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.user.KeyPair
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class SplashViewModel constructor(
	private val encryptedPreferences: EncryptedPreferenceRepository,
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository
) : BaseViewModel() {

	private val _keysLoaded = MutableSharedFlow<Boolean>(replay = 1)
	val keysLoaded = _keysLoaded.asSharedFlow()

	//debug
	fun deletePreviousUser() {
		viewModelScope.launch(Dispatchers.IO) {
			userRepository.deleteMe()

			contactRepository.deleteMe()

			resetKeys()
			loadKeys()
		}
	}

	//debug
	fun resetKeys() {
		encryptedPreferences.userPrivateKey = ""
		encryptedPreferences.userPublicKey = ""
		encryptedPreferences.hash = ""
		encryptedPreferences.signature = ""
	}

	fun loadKeys() {
		viewModelScope.launch {
			//check if keys already exists
			if (encryptedPreferences.userPrivateKey.isBlank() || encryptedPreferences.userPublicKey.isBlank()) {
				//if not, generate and save to shared preferences
				val response = userRepository.getFakeKeyPairFromBE()
				when (response.status) {
					is Status.Success -> response.data?.let { pair ->
						saveKeys(pair)
						_keysLoaded.emit(true)
					}
					is Status.Error -> _keysLoaded.emit(false)
				}
			} else {
				_keysLoaded.emit(true)
			}
		}
	}

	private fun saveKeys(keys: KeyPair) {
		encryptedPreferences.userPrivateKey = keys.privateKey
		encryptedPreferences.userPublicKey = keys.publicKey
	}
}