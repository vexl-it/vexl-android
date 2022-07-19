package cz.cleevio.vexl.chat.chatFragment

import android.view.inputmethod.EditorInfo
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.showSnackbar
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.*
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.FragmentChatBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
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
			viewModel.hasPendingIdentityRevealRequests.collect { pending ->
				if (pending) {
					// TODO finish the correct graphics (I'm afraid it will have to be custom view, that graphics is too complicated for snackbar)
					showSnackbar(
						container = binding.container,
						message = getString(R.string.chat_message_identity_reveal_request),
						duration = Snackbar.LENGTH_INDEFINITE,
						buttonText = R.string.chat_message_identity_reveal_pending_tap,
						action = {
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
					)
				}
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottomWithIME)
		}

		binding.username.text = getUserName(args.communicationRequest)

		binding.sendMessageButton.setOnClickListener {
			sendMessage()
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
	}

	private fun showBottomDialog(dialog: BottomSheetDialogFragment) {
		dialog.show(childFragmentManager, dialog.javaClass.simpleName)
	}

	private fun sendMessage() {
		binding.messageEdit.text.toString().let { message ->
			if (message.isNotBlank()) {
				viewModel.sendMessage(message)
				binding.messageEdit.text?.clear()
			}
		}
	}

	private fun getUserName(communicationRequest: CommunicationRequest): String {
		val name = communicationRequest.message.deanonymizedUser?.name ?: run {
			resources.getString(R.string.marketplace_detail_friend_first)
		}

		return if (communicationRequest.offer?.offerType == "BUY") {
			resources.getString(R.string.marketplace_detail_user_buy, name)
		} else {
			resources.getString(R.string.marketplace_detail_user_sell, name)
		}
	}

}