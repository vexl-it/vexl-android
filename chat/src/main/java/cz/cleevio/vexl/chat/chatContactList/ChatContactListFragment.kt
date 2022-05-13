package cz.cleevio.vexl.chat.chatContactList

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.FragmentChatContactListBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatContactListFragment : BaseFragment(R.layout.fragment_chat_contact_list) {

	private val binding by viewBinding(FragmentChatContactListBinding::bind)
	override val viewModel by viewModel<ChatContactListViewModel>()

	lateinit var adapter: ChatContactListAdapter

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.usersChattedWith.collect { offers ->
				adapter.submitList(offers)
			}
		}
	}

	override fun initView() {

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}

		adapter = ChatContactListAdapter(
			chatWithUser = { user ->
				findNavController().navigate(
					ChatContactListFragmentDirections.proceedToChatFragment(user = user)
				)
			}
		)
		binding.recycler.adapter = adapter

		binding.newRequestsBtn.setOnClickListener {
			findNavController().navigate(
				ChatContactListFragmentDirections.proceedToChatRequestFragment()
			)
		}
	}
}