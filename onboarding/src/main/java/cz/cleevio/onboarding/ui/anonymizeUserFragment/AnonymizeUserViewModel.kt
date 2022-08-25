package cz.cleevio.onboarding.ui.anonymizeUserFragment

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.RandomUtils
import cz.cleevio.network.data.Resource
import cz.cleevio.network.request.user.UserRequest
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AnonymizeUserViewModel constructor(
	val navMainGraphModel: NavMainGraphModel,
	val userRepository: UserRepository,
	private val chatRepository: ChatRepository,
	private val encryptedPreference: EncryptedPreferenceRepository,
) : BaseViewModel() {

	private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.Normal)
	val uiState: StateFlow<UIState> = _uiState.asStateFlow()

	private val _registerUserChannel = Channel<Resource<out Any>>(Channel.CONFLATED)
	val registerUserFlow = _registerUserChannel.receiveAsFlow()

	val currentUser = userRepository.getUserFlow()

	fun anonymizeUser(context: Context) {
		viewModelScope.launch(Dispatchers.Default) {
			val randomImageIndex = RandomUtils.getAvatarIndex()
			val newName = RandomUtils.generateName()
			val newAvatar = RandomUtils.selectRandomImage(context, RandomUtils.getRandomImageDrawableId(randomImageIndex))
			userRepository.storeAnonymousUserData(
				newName,
				randomImageIndex
			)
			//wait until animation is covering the ui
			delay(DELAY)
			_uiState.emit(
				UIState.Anonymized(
					NameWithAvatar(newName, newAvatar)
				)
			)
		}
	}

	fun registerUser(request: UserRequest) {
		viewModelScope.launch(Dispatchers.IO) {
			_registerUserChannel.send(Resource.loading())

			val registerUserResponse = userRepository.registerUser(request)

			if (registerUserResponse.isError()) {
				_registerUserChannel.send(registerUserResponse)
				return@launch
			}

			//create inbox for user
			val inboxResponse = chatRepository.createInbox(
				publicKey = encryptedPreference.userPublicKey
			)

			_registerUserChannel.send(inboxResponse)

			if (inboxResponse.isSuccess()) {
				navMainGraphModel.navigateToGraph(NavMainGraphModel.NavGraph.Contacts)
			}
		}
	}

	data class NameWithAvatar constructor(
		val name: String,
		val avatar: Drawable?
	)

	sealed class UIState {
		object Normal : UIState()
		data class Anonymized constructor(
			val nameWithAvatar: NameWithAvatar
		) : UIState()
	}

	companion object {
		private const val DELAY = 500L
	}
}
