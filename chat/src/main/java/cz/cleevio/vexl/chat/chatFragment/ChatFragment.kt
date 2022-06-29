package cz.cleevio.vexl.chat.chatFragment

import android.view.inputmethod.EditorInfo
import androidx.core.view.updatePadding
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.*
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.repository.model.contact.CommonFriend
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
	}

	override fun initView() {

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottomWithIME)
		}

		args.communicationRequest.let { request ->
			binding.username.text = getUserName(request)
		}

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

		binding.myOfferBtn.setOnClickListener {
			showBottomDialog(MyOfferBottomSheetDialog(args.communicationRequest.offer!!)) // TODO solve double !
		}
		binding.commonFriendsBtn.setOnClickListener {
			val commonFriends = listOf(
				CommonFriend(
					1L,
					"Spongebob Squarepants",
					"FACEBOOK",
					"https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/640px-SpongeBob_SquarePants_character.svg.png"
				),
				CommonFriend(
					2L,
					"Patrick Star",
					"+420 111 111 111",
					"https://upload.wikimedia.org/wikipedia/en/thumb/3/33/Patrick_Star.svg/1200px-Patrick_Star.svg.png"
				),
				CommonFriend(
					3L,
					"Samuel L. Jackson",
					"Facebook",
					"https://i.imgflip.com/4/1480cf.jpg"
				)
			)
			showBottomDialog(CommonFriendsBottomSheetDialog(commonFriends)) // TODO get correct data
		}
		binding.revealIdentityBtn.setOnClickListener {
			showBottomDialog(IdentityRequestBottomSheetDialog())
		}
		binding.deleteChatBtn.setOnClickListener {
			showBottomDialog(DeleteChatBottomSheetDialog())
		}
		binding.blockUserBtn.setOnClickListener {
			showBottomDialog(BlockUserBottomSheetDialog())
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