package cz.cleevio.onboarding.ui.phoneDoneFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhoneDoneViewModel constructor(
	private val navMainGraphModel: NavMainGraphModel
): BaseViewModel() {

	fun startTimer() {
		viewModelScope.launch {
			delay(SCREEN_DELAY)
			navMainGraphModel.navigateToGraph(NavMainGraphModel.NavGraph.OnboardingIdentity)
		}
	}

	private companion object {
		private const val SCREEN_DELAY = 2000L
	}
}
