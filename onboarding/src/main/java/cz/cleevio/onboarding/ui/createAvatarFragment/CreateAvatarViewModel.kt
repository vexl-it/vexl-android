package cz.cleevio.onboarding.ui.createAvatarFragment

import cz.cleevio.core.base.BaseAvatarViewModel
import cz.cleevio.core.utils.NavMainGraphModel
import lightbase.camera.utils.ImageHelper

class CreateAvatarViewModel constructor(
	navMainGraphModel: NavMainGraphModel,
	imageHelper: ImageHelper
) : BaseAvatarViewModel(navMainGraphModel, imageHelper)
