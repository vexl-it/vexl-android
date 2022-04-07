package cz.cleevio.lightspeedskeleton.ui.mainActivity

import cz.cleevio.core.utils.NavMainGraphModel
import lightbase.core.baseClasses.BaseViewModel

class MainViewModel constructor(
	val navMainGraphModel: NavMainGraphModel,
) : BaseViewModel() {

	val navGraphFlow = navMainGraphModel.navGraphFlow
}
