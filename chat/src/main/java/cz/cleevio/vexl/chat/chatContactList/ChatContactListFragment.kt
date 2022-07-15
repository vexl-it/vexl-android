package cz.cleevio.vexl.chat.chatContactList

import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.base.BaseGraphFragment
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.CurrencyPriceChartWidget
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.FragmentChatContactListBinding
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ChatContactListFragment : BaseGraphFragment(R.layout.fragment_chat_contact_list) {

	private val binding by viewBinding(FragmentChatContactListBinding::bind)
	private val chatContactListViewModel by viewModel<ChatContactListViewModel>()

	override var priceChartWidget: CurrencyPriceChartWidget? = null

	lateinit var adapter: ChatContactListAdapter

	override fun bindObservers() {
		super.bindObservers()

		repeatScopeOnStart {
			chatContactListViewModel.usersChattedWith.collect { offers ->
				adapter.submitList(offers)
			}
		}
		repeatScopeOnStart {
			chatContactListViewModel.showRefreshIndicator.collect { shouldShow ->
				binding.progress.isVisible = shouldShow
			}
		}
		repeatScopeOnStart {
			chatContactListViewModel.usersRequestingChat.collect { messages ->
				binding.newRequestsBtn.setImageResource(
					if (messages.isNotEmpty()) {
						R.drawable.ic_users_notification_on
					} else {
						R.drawable.ic_users_notification_off
					}
				)
			}
		}
	}

	override fun initView() {
		priceChartWidget = binding.priceChart

		super.initView()
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top)
			binding.chatsWrapper.updatePadding(bottom = insets.bottom)
		}

		adapter = ChatContactListAdapter(
			chatWithUser = { userWithMessage ->
				findNavController().navigate(
//				todo: fix `!!`
					ChatContactListFragmentDirections.proceedToChatFragment(
						communicationRequest = CommunicationRequest(
							message = userWithMessage.message,
							offer = userWithMessage.offer
						)
					)
				)
			}
		)
		binding.recycler.adapter = adapter

		binding.newRequestsBtn.setOnClickListener {
			findNavController().navigate(
				ChatContactListFragmentDirections.proceedToChatRequestFragment()
			)
		}

		binding.chatTypeRadiogroup.setOnCheckedChangeListener { _, id ->
			when (id) {
				R.id.all_radio -> chatContactListViewModel.filter(ChatContactListViewModel.FilterType.ALL)
				R.id.buyers_radio -> chatContactListViewModel.filter(ChatContactListViewModel.FilterType.BUYERS)
				R.id.sellers_radio -> chatContactListViewModel.filter(ChatContactListViewModel.FilterType.SELLERS)
				else -> {
					Timber.e("Unknown chat filter ID! '$id'")
					chatContactListViewModel.filter(ChatContactListViewModel.FilterType.ALL)
				}
			}
		}
	}

	override fun onResume() {
		super.onResume()

		chatContactListViewModel.refreshChats()
		chatContactListViewModel.refreshChatRequests()
	}
}