package cz.cleeevio.onboarding.avatarFragment

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.base.BaseAvatarViewModel
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.network.data.Status
import cz.cleevio.network.request.user.UserAvatar
import cz.cleevio.repository.model.user.User
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lightbase.camera.ui.takePhotoFragment.TakePhotoResult
import timber.log.Timber
import java.io.ByteArrayOutputStream

class AvatarViewModel constructor(
	private val userRepository: UserRepository,
	private val chatRepository: ChatRepository,
	private val encryptedPreference: EncryptedPreferenceRepository,
	navMainGraphModel: NavMainGraphModel
) : BaseAvatarViewModel(navMainGraphModel) {

	private val _user = MutableStateFlow<User?>(null)
	val user = _user.asStateFlow()

	fun registerUser(
		username: String,
		contentResolver: ContentResolver
	) {
		viewModelScope.launch(Dispatchers.IO) {
			val profileUri = profileImageUri.value

			val response = userRepository.registerUser(
				username = username,
				avatar = if (profileUri != null) {
					UserAvatar(
						data = super.getAvatarData(profileUri, contentResolver),
						extension = IMAGE_EXTENSION
					)
				} else null
			)
			//create inbox for user
			val inboxResponse = chatRepository.createInbox(
				publicKey = encryptedPreference.userPublicKey
			)
			when (inboxResponse.status) {
				Status.Success -> {
					_user.emit(response.data)
				}
				Status.Error -> {
					//todo: add error handling?
				}
			}
		}
	}
}