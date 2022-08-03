package cz.cleevio.onboarding.ui.phoneDoneFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhoneDoneViewModel : BaseViewModel() {

	private val _goToNextScreen = MutableStateFlow(false)
	val goToNextScreen = _goToNextScreen.asStateFlow()

	fun startTimer() {
		viewModelScope.launch {
			delay(SCREEN_DELAY)
			_goToNextScreen.emit(true)
		}
	}

	private companion object {
		private const val SCREEN_DELAY = 2000L
	}
}
