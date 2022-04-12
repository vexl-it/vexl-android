package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentOffersBinding
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

sealed class OffersBaseFragment : BaseFragment(R.layout.fragment_offers) {

	override val viewModel by viewModel<OffersViewModel>()
	val binding by viewBinding(FragmentOffersBinding::bind)
	private lateinit var adapter: OffersAdapter

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.buyOffers.collect { offers ->
				adapter.submitList(offers)
			}
		}
	}

	override fun initView() {
		adapter = OffersAdapter()
		binding.offerList.adapter = adapter

		viewModel.getData()
	}

}