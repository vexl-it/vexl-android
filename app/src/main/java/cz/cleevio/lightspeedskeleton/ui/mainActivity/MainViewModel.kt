package cz.cleevio.lightspeedskeleton.ui.mainActivity

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.entity.MessageKeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.model.chat.ChatListUser
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel constructor(
	val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	val chatRepository: ChatRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	val navGraphFlow = navMainGraphModel.navGraphFlow

	private val _navigateToChatDetail = Channel<ChatListUser>(Channel.CONFLATED)
	val navigateToChatDetail = _navigateToChatDetail.receiveAsFlow()

	private val _bottomBarAnimation = MutableStateFlow(false)

	@OptIn(FlowPreview::class)
	val bottomBarAnimation = _bottomBarAnimation.asStateFlow()
		.debounce(DEBOUNCE_DELAY)

	fun setBottomBarState(visible: Boolean) {
		viewModelScope.launch(Dispatchers.Default) {
			_bottomBarAnimation.emit(visible)
		}
	}

	fun goToChatDetail(inboxKey: String, senderKey: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val userWithMessage = chatRepository.getOneChatUser(
				messageKeyPair = MessageKeyPair(
					senderPublicKey = senderKey,
					recipientPublicKey = inboxKey
				)
			)
			userWithMessage?.let {
				Timber.tag("ASDX").d("looking for controller and safe navigating")
				_navigateToChatDetail.send(userWithMessage)
			}
		}
	}

	companion object {
		private const val DEBOUNCE_DELAY = 100L
	}
}
