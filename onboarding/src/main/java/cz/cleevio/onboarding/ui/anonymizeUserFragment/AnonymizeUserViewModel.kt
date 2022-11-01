package cz.cleevio.onboarding.ui.anonymizeUserFragment

import android.content.ContentResolver
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.base.BaseAvatarViewModel
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.network.data.Resource
import cz.cleevio.network.request.user.UserAvatar
import cz.cleevio.repository.RandomUtils
import cz.cleevio.repository.model.user.User
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import lightbase.camera.utils.ImageHelper

class AnonymizeUserViewModel constructor(
	navMainGraphModel: NavMainGraphModel,
	imageHelper: ImageHelper,
	val userRepository: UserRepository,
	private val chatRepository: ChatRepository,
	private val encryptedPreference: EncryptedPreferenceRepository,
) : BaseAvatarViewModel(navMainGraphModel, imageHelper) {

	private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.Normal)
	val uiState: StateFlow<UIState> = _uiState.asStateFlow()

	private val _registerUserChannel = Channel<Resource<out Any>>(Channel.CONFLATED)
	val registerUserFlow = _registerUserChannel.receiveAsFlow()

	val currentUser = userRepository.getUserFlow()
	var anonymousUsername: String? = null
	var anonymousAvatarIndex: Int = 0

	fun anonymizeUser(context: Context) {
		viewModelScope.launch(Dispatchers.Default) {
			anonymousUsername = RandomUtils.generateName()
			anonymousAvatarIndex = RandomUtils.getAvatarIndex()
			val newAvatar = RandomUtils.selectRandomImage(context, RandomUtils.getRandomImageDrawableId(anonymousAvatarIndex))

			//wait until animation is covering the ui
			delay(DELAY)
			_uiState.emit(
				UIState.Anonymized(
					NameWithAvatar(anonymousUsername, newAvatar)
				)
			)
		}
	}

	fun registerUser(
		username: String,
		avatarUri: Uri?,
		contentResolver: ContentResolver
	) {
		viewModelScope.launch(Dispatchers.IO) {
			_registerUserChannel.send(Resource.loading())

			val avatar = if (avatarUri != null) {
				UserAvatar(
					data = super.getAvatarData(avatarUri, contentResolver),
					extension = IMAGE_EXTENSION
				)
			} else null

			val user = User(
				id = null,
				username = username,
				anonymousUsername = anonymousUsername,
				avatar = null,
				avatarBase64 = avatar?.data,
				anonymousAvatarImageIndex = anonymousAvatarIndex,
				publicKey = encryptedPreference.userPublicKey,
				finishedOnboarding = false
			)
			userRepository.createUser(user)

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
		val name: String?,
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
