package cz.cleeevio.onboarding.initPhoneFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel
import timber.log.Timber


class InitPhoneViewModel constructor(
	private val userRepository: UserRepository
) : BaseViewModel() {

	fun sendPhoneNumber(phoneNumber: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = userRepository.authStepOne(phoneNumber)

			Timber.tag("ASDX").d("${response.data}")
		}
	}
}