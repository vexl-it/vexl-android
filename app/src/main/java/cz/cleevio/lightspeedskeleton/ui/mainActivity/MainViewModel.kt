package cz.cleevio.lightspeedskeleton.ui.mainActivity

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class MainViewModel constructor(
	private val userRepository: UserRepository,
	private val navMainGraphModel: NavMainGraphModel,
) : BaseViewModel() {

	val navGraphFlow = navMainGraphModel.navGraphFlow

	fun clearData() {
		viewModelScope.launch(Dispatchers.IO) {
			// TODO clear repositories
		}

		navMainGraphModel.navigateToGraph(
			NavMainGraphModel.NavGraph.Onboarding
		)
	}
}
