package cz.cleevio.onboarding.ui.createAvatarFragment

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import lightbase.camera.utils.ImageHelper

class CreateAvatarViewModel constructor(
	private val userRepository: UserRepository,
	private val chatRepository: ChatRepository,
	private val encryptedPreference: EncryptedPreferenceRepository,
	navMainGraphModel: NavMainGraphModel,
	imageHelper: ImageHelper
) : BaseAvatarViewModel(navMainGraphModel, imageHelper) {

	private val _user = Channel<User?>()
	val user = _user.receiveAsFlow()

	private val _loading = Channel<Boolean>()
	val loading = _loading.receiveAsFlow()

	fun registerUser(
		username: String,
		contentResolver: ContentResolver
	) {
		viewModelScope.launch(Dispatchers.IO) {
			_loading.send(true)
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
					_user.send(response.data)
				}
				Status.Error -> {
					//todo: add error handling?
				}
			}

			_loading.send(false)
		}
	}
}
