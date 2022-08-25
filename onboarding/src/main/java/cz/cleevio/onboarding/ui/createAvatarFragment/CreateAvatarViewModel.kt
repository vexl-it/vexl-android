package cz.cleevio.onboarding.ui.createAvatarFragment

import android.content.ContentResolver
import cz.cleevio.core.base.BaseAvatarViewModel
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.network.request.user.UserAvatar
import cz.cleevio.network.request.user.UserRequest
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
