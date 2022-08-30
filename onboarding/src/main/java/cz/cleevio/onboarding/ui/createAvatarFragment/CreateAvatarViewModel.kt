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
) : BaseAvatarViewModel(navMainGraphModel, imageHelper) {}
