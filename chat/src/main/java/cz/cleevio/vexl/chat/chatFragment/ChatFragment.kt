package cz.cleevio.vexl.chat.chatFragment

import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import cz.cleevio.core.utils.BuySellColorizer
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.showSnackbar
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.*
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.FragmentChatBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForIMEInset
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ChatFragment : BaseFragment(R.layout.fragment_chat) {

	private val binding by viewBinding(FragmentChatBinding::bind)
	override val viewModel by viewModel<ChatViewModel> { parametersOf(args.communicationRequest) }

	private val args by navArgs<ChatFragmentArgs>()

	lateinit var adapter: ChatMessagesAdapter

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.messages.collect { messages ->
				adapter.submitList(messages)
			}
		}
		repeatScopeOnStart {
			viewModel.chatUserIdentity.collect { chatUserIdentity ->
				binding.profileImage.load(chatUserIdentity?.avatar) {
					crossfade(true)
					fallback(R.drawable.random_avatar_5)
					error(R.drawable.random_avatar_5)
					placeholder(R.drawable.random_avatar_5)
				}
				setupColoredTitle(chatUserIdentity?.name ?: "")
			}
		}
		repeatScopeOnStart {
			viewModel.hasPendingIdentityRevealRequests.collect { pending ->
				binding.identityRevealRequestedWrapper.isVisible = pending
			}
		}
		repeatScopeOnStart {
			viewModel.canRequestIdentity.collect { canRequestIdentity ->
				binding.revealIdentityBtn.isEnabled = canRequestIdentity
				binding.revealIdentityBtn.setBackgroundColor(
					if (canRequestIdentity) {
						requireContext().getColor(R.color.gray_1)
					} else {
						requireContext().getColor(R.color.gray_2)
					}
				)
			}
		}
	}

	override fun initView() {
		val name = args.communicationRequest.message.deanonymizedUser?.name ?: run {
			getString(R.string.marketplace_detail_friend_first)
		}

		setupColoredTitle(name)

		binding.sendMessageButton.setOnClickListener {
			sendMessage()
		}

		binding.identityRevealRequestedButton.setOnClickListener {
			showBottomDialog(
				RevealIdentityBottomSheetDialog(
					onApprove = {
						viewModel.resolveIdentityRevealRequest(true)
					},
					onReject = {
						viewModel.resolveIdentityRevealRequest(false)
					}
				)
			)
		}

		binding.messageEdit.setOnEditorActionListener { v, actionId, event ->
			if (actionId == EditorInfo.IME_ACTION_SEND) {
				sendMessage()
			}
			false
		}

		adapter = ChatMessagesAdapter()
		binding.chatRv.adapter = adapter

		binding.close.setOnClickListener {
			//close this screen
			findNavController().popBackStack()
		}
		binding.myOfferBtn.setOnClickListener {
			showBottomDialog(MyOfferBottomSheetDialog(args.communicationRequest.offer!!)) // TODO solve double !
		}
		binding.commonFriendsBtn.setOnClickListener {
			showBottomDialog(CommonFriendsBottomSheetDialog(args.communicationRequest.offer?.commonFriends.orEmpty()))
		}
		binding.revealIdentityBtn.setOnClickListener {
			showBottomDialog(IdentityRequestBottomSheetDialog(
				senderPublicKey = viewModel.senderPublicKey,
				receiverPublicKey = viewModel.receiverPublicKey,
				inboxPublicKey = viewModel.communicationRequest.message.inboxPublicKey,
				onSendSuccess = {
					showSnackbar(
						container = binding.container,
						message = getString(R.string.chat_identity_reveal_sent),
						duration = Snackbar.LENGTH_INDEFINITE,
						buttonText = R.string.chat_message_identity_reveal_pending_ok,
						action = {}
					)
				}
			))
		}
		binding.deleteChatBtn.setOnClickListener {
			showBottomDialog(
				DeleteChatBottomSheetDialog(
					senderPublicKey = viewModel.senderPublicKey,
					receiverPublicKey = viewModel.receiverPublicKey,
					inboxPublicKey = viewModel.communicationRequest.message.inboxPublicKey
				)
			)
		}
		binding.blockUserBtn.setOnClickListener {
			showBottomDialog(
				BlockUserBottomSheetDialog(
					senderPublicKey = viewModel.senderPublicKey, publicKeyToBlock = viewModel.receiverPublicKey
				)
			)
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top)
		}

		listenForIMEInset(binding.submitMessageWrapper) { insets ->
			binding.submitMessageWrapper.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				bottomMargin = insets
			}
		}
	}

	private fun setupColoredTitle(name: String) {
		val isRequested = args.communicationRequest.offer?.isRequested == true

		if (args.communicationRequest.offer?.offerType == "BUY") {
			BuySellColorizer.colorizeTransactionType(
				getString(R.string.marketplace_detail_user_buy, name),
				name,
				binding.username,
				if (isRequested) R.color.gray_3 else R.color.green_100
			)
		} else {
			BuySellColorizer.colorizeTransactionType(
				getString(R.string.marketplace_detail_user_sell, name),
				name,
				binding.username,
				if (isRequested) R.color.gray_3 else R.color.pink_100
			)
		}
	}

	private fun showBottomDialog(dialog: BottomSheetDialogFragment) {
		if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
			dialog.show(childFragmentManager, dialog.javaClass.simpleName)
		}
	}

	private fun sendMessage() {
		binding.messageEdit.text.toString().let { message ->
			if (message.isNotBlank()) {
				viewModel.sendMessage(message)
				binding.messageEdit.text?.clear()
			}
		}
	}
}
