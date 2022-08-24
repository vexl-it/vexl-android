package cz.cleevio.onboarding.ui.createAvatarFragment

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.base.BaseAvatarViewModel
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.network.data.Status
import cz.cleevio.network.request.user.UserAvatar
import cz.cleevio.network.request.user.UserRequest
import cz.cleevio.repository.model.user.User
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import lightbase.camera.utils.ImageHelper

class CreateAvatarViewModel constructor(
	navMainGraphModel: NavMainGraphModel,
	imageHelper: ImageHelper
) : BaseAvatarViewModel(navMainGraphModel, imageHelper) {

	fun getUserRequest(
		username: String,
		contentResolver: ContentResolver
	): UserRequest {
		val profileUri = profileImageUri.value
		val avatar = if (profileUri != null) {
			UserAvatar(
				data = super.getAvatarData(profileUri, contentResolver),
				extension = IMAGE_EXTENSION
			)
		} else null

		return UserRequest(
			username = username,
			avatar = avatar
		)
	}
}
