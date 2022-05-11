package cz.cleevio.vexl.chat.chatFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.navArgs
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.FragmentChatBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ChatFragment : BaseFragment(R.layout.fragment_chat) {

	private val binding by viewBinding(FragmentChatBinding::bind)
	override val viewModel by viewModel<ChatViewModel> { parametersOf(args.user) }

	private val args by navArgs<ChatFragmentArgs>()

	override fun bindObservers() {

	}

	override fun initView() {

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}

		binding.username.text = viewModel.user.username
	}

}