package cz.cleevio.lightspeedskeleton.ui.splashFragment

import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.KeyPairCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class SplashViewModel constructor(
	private val encryptedPreferences: EncryptedPreferenceRepository,
	private val userRepository: UserRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	private val _keysLoaded = MutableSharedFlow<Boolean>(replay = 1)
	val keysLoaded = _keysLoaded.asSharedFlow()

	val userFlow = userRepository.getUserFlow()

	//debug
	fun deletePreviousUserAndLoadKeys() {
		viewModelScope.launch(Dispatchers.IO) {
			resetKeys()
			loadKeys()
		}
	}

	//debug
	private fun resetKeys() {
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
				val keyPair = KeyPairCryptoLib.generateKeyPair()
				saveKeys(keyPair)
				_keysLoaded.emit(true)
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