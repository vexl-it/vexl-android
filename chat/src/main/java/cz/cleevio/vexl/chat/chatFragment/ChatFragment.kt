package cz.cleevio.vexl.chat.chatFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.navArgs
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
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
	}

	override fun initView() {

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}

		args.communicationRequest.let { request ->
			binding.username.text = getUserName(request)
		}

		binding.sendMessageButton.setOnClickListener {
			binding.messageEdit.text.toString().let { message ->
				if (message.isNotBlank()) {
					viewModel.sendMessage(message)
					binding.messageEdit.text?.clear()
				}
			}
		}

		adapter = ChatMessagesAdapter()
		binding.chatRv.adapter = adapter
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