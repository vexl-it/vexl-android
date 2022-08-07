package cz.cleevio.profile.editAvatarFragment

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
import cz.cleevio.core.base.BaseAvatarViewModel
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lightbase.camera.utils.ImageHelper

class EditAvatarViewModel constructor(
	private val userRepository: UserRepository,
	navMainGraphModel: NavMainGraphModel,
	imageHelper: ImageHelper
) : BaseAvatarViewModel(navMainGraphModel, imageHelper) {

	val userFlow = userRepository.getUserFlow()
		.stateIn(viewModelScope, SharingStarted.Lazily, null)

	private val _wasSuccessful = Channel<Boolean>(Channel.CONFLATED)
	val wasSuccessful = _wasSuccessful.receiveAsFlow()

	fun editAvatar(contentResolver: ContentResolver) {
		viewModelScope.launch(Dispatchers.IO) {
			val profileUri = profileImageUri.value

			if (profileUri != null) {
				userRepository.updateUser(
					avatar = super.getAvatarData(profileUri, contentResolver),
					avatarImageExtension = IMAGE_EXTENSION
				).let {
					_wasSuccessful.send(it.isSuccess())
				}
			} else {
				_wasSuccessful.send(false)
			}
		}
	}
}
