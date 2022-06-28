package cz.cleeevio.onboarding.verifyPhoneFragment

import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.EcdsaCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.UserUtils
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.user.ConfirmCode
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class VerifyPhoneViewModel constructor(
	val phoneNumber: String,
	val verificationId: Long,
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	private val encryptedPreference: EncryptedPreferenceRepository,
	private val userUtils: UserUtils,
) : BaseViewModel() {

	private val _verificationChannel = Channel<Resource<ConfirmCode>>(Channel.CONFLATED)
	val verificationChannel: Flow<Resource<ConfirmCode>> = _verificationChannel.receiveAsFlow()

	private val _errorFlow = MutableSharedFlow<ErrorIdentification>()
	val errorFlow = _errorFlow.asSharedFlow()

	fun sendVerificationCode(verificationCode: String) {
		viewModelScope.launch(Dispatchers.IO) {
			_verificationChannel.send(Resource.loading())
			val response = userRepository.authStepTwo(verificationCode, verificationId)
			when (response.status) {
				is Status.Success -> response.data?.challenge?.let { challenge ->
					val signature = EcdsaCryptoLib.sign(
						KeyPair(
							privateKey = encryptedPreference.userPrivateKey,
							publicKey = encryptedPreference.userPublicKey
						), challenge
					)

					solveChallenge(signature) {
						_verificationChannel.send(response)
					}
				}
				is Status.Error -> {
					_errorFlow.emit(response.errorIdentification)
				}
			}
		}
	}

	private fun solveChallenge(signature: String, onSuccess: suspend () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = userRepository.authStepThree(signature)
			when (response.status) {
				is Status.Success -> {
					response.data?.let { data ->
						encryptedPreference.signature = data.signature
						encryptedPreference.hash = data.hash
					}
					registerWithContactsService(onSuccess)
				}
				is Status.Error -> {
					_errorFlow.emit(response.errorIdentification)
				}
			}
		}
	}

	private fun registerWithContactsService(onSuccess: suspend () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = contactRepository.registerUser()
			when (response.status) {
				is Status.Success -> onSuccess()
			}
		}
	}

	fun resetKeys() {
		userUtils.resetKeys()
	}
}