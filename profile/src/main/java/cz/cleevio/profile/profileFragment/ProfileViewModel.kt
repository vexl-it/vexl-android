package cz.cleevio.profile.profileFragment

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.LogUtils
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.network.NetworkError
import cz.cleevio.repository.model.Currency
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class ProfileViewModel constructor(
	private val userRepository: UserRepository,
	val remoteConfig: FirebaseRemoteConfig,
	val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	val navMainGraphModel: NavMainGraphModel,
	val logUtils: LogUtils,
	val networkError: NetworkError
) : BaseViewModel() {

	val userFlow = userRepository.getUserFlow()
	val logs = logUtils.logFlow

	val areScreenshotsAllowed
		get() = encryptedPreferenceRepository.areScreenshotsAllowed

	private val _contactsNumber = MutableStateFlow<Int>(0)
	val contactsNumber = _contactsNumber.asStateFlow()

	private val hasPermissionsChannel = Channel<Boolean>(Channel.CONFLATED)
	val hasPermissionsEvent = hasPermissionsChannel.receiveAsFlow()

	fun updateHasReadContactPermissions(hasPermissions: Boolean) {
		hasPermissionsChannel.trySend(hasPermissions)
	}

	fun updateAllowScreenshotsSettings() {
		encryptedPreferenceRepository.areScreenshotsAllowed = !encryptedPreferenceRepository.areScreenshotsAllowed
	}

	fun setCurrency(currency: Currency) {
		encryptedPreferenceRepository.selectedCurrency = currency.name
	}

	fun logout() {
		networkError.sendLogout()
	}
}
