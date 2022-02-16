package cz.cleeevio.onboarding.codeValidationFragment

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel
import timber.log.Timber

class CodeValidationViewModel : BaseViewModel() {

	private val resendCodeSuccess = Channel<Boolean>(Channel.CONFLATED)
	val resendCodeSuccessEvent = resendCodeSuccess.receiveAsFlow()

	fun resendCode(email: String) {
		Timber.w(email)
		viewModelScope.launch(Dispatchers.IO) {
			delay(FAKE_DELAY)
			resendCodeSuccess.send(true)
		}
	}

	companion object {
		private const val FAKE_DELAY = 1000L
	}
}
