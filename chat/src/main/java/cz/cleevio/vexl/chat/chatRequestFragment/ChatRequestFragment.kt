package cz.cleevio.vexl.chat.chatRequestFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.PagerSnapHelper
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.repository.model.user.User
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.FragmentChatRequestBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatRequestFragment : BaseFragment(R.layout.fragment_chat_request) {

	private val binding by viewBinding(FragmentChatRequestBinding::bind)
	override val viewModel by viewModel<ChatRequestViewModel>()

	lateinit var adapter: ChatRequestAdapter

	private val onAccept: (User) -> Unit = { user ->
		//move to chat
		findNavController().navigate(
			ChatRequestFragmentDirections.proceedToChatFragment(user)
		)
	}
	private val onBlock: (User) -> Unit = {
		//#TODO
		//mark user as blocked?

		//reload users?

		//and show next?

	}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.usersRequestingChat.collect { messages ->
				binding.title.text = resources.getString(R.string.chat_request_main_title, messages.size)
				adapter.submitList(messages)
			}
		}
	}

	override fun initView() {
		adapter = ChatRequestAdapter()
		binding.requestsRecyclerView.adapter = adapter

		PagerSnapHelper().attachToRecyclerView(binding.requestsRecyclerView)

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}

		binding.acceptBtn.setOnClickListener {
			// TODO
		}

		binding.declineBtn.setOnClickListener {
			// TODO
		}
	}
}