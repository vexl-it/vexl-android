package cz.cleevio.lightspeedskeleton.ui.splashFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class SplashViewModel constructor(
	private val encryptedPreferences: EncryptedPreferenceRepository,
	private val userRepository: UserRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	val userFlow = userRepository.getUserFlow()

	//debug
	fun deletePreviousUserAndLoadKeys() {
		viewModelScope.launch(Dispatchers.IO) {
			resetKeys()
		}
	}

	//debug
	private fun resetKeys() {
		encryptedPreferences.userPrivateKey = ""
		encryptedPreferences.userPublicKey = ""
		encryptedPreferences.hash = ""
		encryptedPreferences.signature = ""
	}
}