package cz.cleevio.vexl.chat.chatRequestFragment

import androidx.core.view.updatePadding
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.FragmentChatRequestBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatRequestFragment : BaseFragment(R.layout.fragment_chat_request) {

	private val binding by viewBinding(FragmentChatRequestBinding::bind)
	override val viewModel by viewModel<ChatRequestViewModel>()


	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.usersRequestingChat.collect { users ->
				//todo: show stacked cards
			}
		}
	}

	override fun initView() {

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}
	}
}