package cz.cleevio.vexl.chat.chatContactList

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.CurrencyPriceChartViewModel
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.FragmentChatContactListBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ChatContactListFragment : BaseFragment(R.layout.fragment_chat_contact_list) {

	private val binding by viewBinding(FragmentChatContactListBinding::bind)
	override val viewModel by viewModel<ChatContactListViewModel>()
	private val priceChartViewModel by viewModel<CurrencyPriceChartViewModel>()

	lateinit var adapter: ChatContactListAdapter

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.usersChattedWith.collect { offers ->
				adapter.submitList(offers)
			}
		}
		repeatScopeOnStart {
			priceChartViewModel.currentCryptoCurrencyPrice.collect { currentCryptoCurrencyPrice ->
				binding.priceChart.setupData(currentCryptoCurrencyPrice)
			}
		}
	}

	override fun initView() {

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top)
			binding.chatsWrapper.updatePadding(bottom = insets.bottom)
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

		binding.chatTypeRadiogroup.setOnCheckedChangeListener { _, id ->
			when (id) {
				R.id.all_radio -> viewModel.filter(ChatContactListViewModel.FilterType.ALL)
				R.id.buyers_radio -> viewModel.filter(ChatContactListViewModel.FilterType.BUYERS)
				R.id.sellers_radio -> viewModel.filter(ChatContactListViewModel.FilterType.SELLERS)
				else -> {
					Timber.e("Unknown chat filter ID! '$id'")
					viewModel.filter(ChatContactListViewModel.FilterType.ALL)
				}
			}
		}
	}
}