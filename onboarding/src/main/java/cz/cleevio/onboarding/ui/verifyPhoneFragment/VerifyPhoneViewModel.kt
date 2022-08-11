package cz.cleevio.onboarding.ui.verifyPhoneFragment

import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.EcdsaCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.UserUtils
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.onboarding.ui.initPhoneFragment.InitPhoneSuccess
import cz.cleevio.repository.model.user.ConfirmCode
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

	private val _phoneNumberSuccess = Channel<InitPhoneSuccess>(Channel.CONFLATED)
	val phoneNumberSuccess = _phoneNumberSuccess.receiveAsFlow()

	private val _countDownState: MutableStateFlow<CountDownState> =
		MutableStateFlow(CountDownState.Counting(COUNTDOWN_LENGTH))
	val countdownState: StateFlow<CountDownState> = _countDownState.asStateFlow()

	private val _errorFlow = MutableSharedFlow<ErrorIdentification>()
	val errorFlow = _errorFlow.asSharedFlow()

	private val countDownTimer = CoroutineScope(viewModelScope.coroutineContext)

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
					//todo: resend authOne
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

	fun restartCountDown() {
		countDownTimer.launch {
			// TODO call backend for new sms

			val response = userRepository.authStepOne(phoneNumber)
			when (response.status) {
				is Status.Success -> response.data?.let { confirmPhone ->
					_phoneNumberSuccess.send(
						InitPhoneSuccess(
							phoneNumber = phoneNumber,
							confirmPhone = confirmPhone
						)
					)
				}
			}


			repeat(COUNTDOWN_LENGTH / COUNTDOWN_STEP) {
				_countDownState.emit(CountDownState.Counting(COUNTDOWN_LENGTH - it * COUNTDOWN_STEP))
				delay(COUNTDOWN_STEP.toLong())
			}
			_countDownState.emit(CountDownState.Finished)
		}
	}

	sealed class CountDownState {
		data class Counting(
			val timeLeftInMillis: Int
		) : CountDownState()

		object Finished : CountDownState()
	}

	companion object {
		private const val COUNTDOWN_LENGTH = 30_000
		private const val COUNTDOWN_STEP = 500
	}
}
