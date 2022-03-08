package cz.cleeevio.onboarding.initPhoneFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.user.ConfirmPhone
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel
import timber.log.Timber

class InitPhoneViewModel constructor(
	private val userRepository: UserRepository
) : BaseViewModel() {

	private val _phoneNumberSuccess = Channel<InitPhoneSuccess>(Channel.CONFLATED)
	val phoneNumberSuccess = _phoneNumberSuccess.receiveAsFlow()

	fun sendPhoneNumber(phoneNumber: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = userRepository.authStepOne(phoneNumber)
			Timber.tag("ASDX").d("${response.data}")

			when (response.status) {
				is Status.Success -> {
					response.data?.let { confirmPhone ->
						_phoneNumberSuccess.send(
							InitPhoneSuccess(
								phoneNumber = phoneNumber,
								confirmPhone = confirmPhone
							)
						)
					}
				}
			}
		}
	}
}

data class InitPhoneSuccess constructor(
	val phoneNumber: String,
	val confirmPhone: ConfirmPhone
)