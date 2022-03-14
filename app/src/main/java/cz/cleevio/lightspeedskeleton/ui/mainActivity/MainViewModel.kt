package cz.cleevio.lightspeedskeleton.ui.mainActivity

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.entity.UserEntity
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class MainViewModel constructor(
	private val userRepository: UserRepository,
	private val navMainGraphModel: NavMainGraphModel,
) : BaseViewModel() {

	val navGraphFlow = navMainGraphModel.navGraphFlow

	private val _localUser = MutableSharedFlow<UserEntity?>(replay = 1)
	val localUser = _localUser.asSharedFlow()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			userRepository.getUserFlow().collect {
				_localUser.emit(it)
			}
		}
	}

	fun clearData() {
		viewModelScope.launch(Dispatchers.IO) {
			// TODO clear repositories
		}

		navMainGraphModel.navigateToGraph(
			NavMainGraphModel.NavGraph.Onboarding
		)
	}
}
