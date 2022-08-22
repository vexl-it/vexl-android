package cz.cleevio.lightspeedskeleton.ui.mainActivity

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import cz.cleevio.cache.entity.MessageKeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.vexl.chat.chatContactList.ChatContactListFragmentDirections
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainViewModel constructor(
	val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	val chatRepository: ChatRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	val navGraphFlow = navMainGraphModel.navGraphFlow

	private val _bottomBarAnimation = MutableStateFlow(false)

	@OptIn(FlowPreview::class)
	val bottomBarAnimation = _bottomBarAnimation.asStateFlow()
		.debounce(DEBOUNCE_DELAY)

	fun setBottomBarState(visible: Boolean) {
		viewModelScope.launch(Dispatchers.Default) {
			_bottomBarAnimation.emit(visible)
		}
	}

	fun goToChatDetail(navController: NavController, inboxKey: String, senderKey: String) {
		viewModelScope.launch(Dispatchers.IO) {
			Timber.tag("ASDX").d("keys not null")
			//this is here only for debug TODO: remove
			val users = chatRepository.loadChatUsers()

			Timber.tag("ASDX").d("users loaded ${users.size}")

			val userWithMessage = chatRepository.getOneChatUser(
				messageKeyPair = MessageKeyPair(
					senderPublicKey = senderKey ?: "",
					recipientPublicKey = inboxKey ?: ""
				)
			) ?: users.firstOrNull()

			Timber.tag("ASDX").d("userWithMessage $userWithMessage")

			//TODO: select correct one
			//val userWithMessage = users.find { it.offer } ?: users.firstOrNull()

			userWithMessage?.let {
				Timber.tag("ASDX").d("looking for controller and safe navigating")
				withContext(Dispatchers.Main) {
					navController.safeNavigateWithTransition(
						ChatContactListFragmentDirections.proceedToChatFragment(
							communicationRequest = CommunicationRequest(
								message = userWithMessage.message,
								offer = userWithMessage.offer
							)
						)
					)
				}
			}
		}
	}

	companion object {
		private const val DEBOUNCE_DELAY = 100L
	}
}
