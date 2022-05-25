package cz.cleeevio.vexl.marketplace.marketplaceFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentMarketplaceBinding
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.CurrencyPriceChartViewModel
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class MarketplaceFragment : BaseFragment(R.layout.fragment_marketplace) {


	private val binding by viewBinding(FragmentMarketplaceBinding::bind)
	override val viewModel by viewModel<CurrencyPriceChartViewModel>()
	private val marketplaceViewModel by viewModel<MarketplaceViewModel>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.currentCryptoCurrencyPrice.collect { currentCryptoCurrencyPrice ->
				binding.priceChart.setupData(currentCryptoCurrencyPrice)
			}
		}
	}

	override fun initView() {

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top)
			binding.marketplaceOffersWrapper.updatePadding(bottom = insets.bottom)
		}

		binding.marketplaceViewpager.adapter = MarketplacePagerAdapter(
			fragment = this,
			navigateToFilters = {
				findNavController().navigate(
					MarketplaceFragmentDirections.proceedToFiltersFragment()
				)
			},
			navigateToNewOffer = {
				findNavController().navigate(
					MarketplaceFragmentDirections.proceedToNewOfferFragment()
				)
			},
			requestOffer = { offerId ->
				findNavController().navigate(
					MarketplaceFragmentDirections.proceedToRequestOfferFragment(offerId = offerId)
				)
			}
		)

		TabLayoutMediator(binding.marketplaceTabLayout, binding.marketplaceViewpager) { tab, position ->
			when (position) {
				0 -> tab.setText(R.string.marketplace_buy)
				1 -> tab.setText(R.string.marketplace_sell)
				else -> tab.text = "Error"
			}

		}.attach()

		marketplaceViewModel.syncOffers()
	}

}