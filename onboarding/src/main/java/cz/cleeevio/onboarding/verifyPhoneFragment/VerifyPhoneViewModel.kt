package cz.cleeevio.onboarding.verifyPhoneFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.user.ConfirmCode
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class VerifyPhoneViewModel constructor(
	val phoneNumber: String,
	val verificationId: Long,
	private val userRepository: UserRepository
) : BaseViewModel() {

	private val _verificationChannel = Channel<Resource<ConfirmCode>>(Channel.CONFLATED)
	val verificationChannel: Flow<Resource<ConfirmCode>> = _verificationChannel.receiveAsFlow()

	fun sendVerificationCode(verificationCode: String) {
		viewModelScope.launch(Dispatchers.IO) {
			_verificationChannel.send(Resource.loading())
			_verificationChannel.send(userRepository.authStepTwo(verificationCode, verificationId))
		}
	}
}