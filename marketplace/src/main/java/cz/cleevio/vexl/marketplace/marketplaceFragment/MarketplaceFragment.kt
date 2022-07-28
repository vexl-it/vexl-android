package cz.cleevio.vexl.marketplace.marketplaceFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import cz.cleevio.core.base.BaseGraphFragment
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.CurrencyPriceChartWidget
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.marketplace.R
import cz.cleevio.vexl.marketplace.databinding.FragmentMarketplaceBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MarketplaceFragment : BaseGraphFragment(R.layout.fragment_marketplace) {

	private val binding by viewBinding(FragmentMarketplaceBinding::bind)
	private val marketplaceViewModel by viewModel<MarketplaceViewModel>()

	override var priceChartWidget: CurrencyPriceChartWidget? = null

	override fun onResume() {
		super.onResume()
		viewModel.syncMarketData()
	}

	override fun onStart() {
		super.onStart()

		marketplaceViewModel.syncAllMessages()
		marketplaceViewModel.syncMyGroupsData()
	}

	override fun initView() {
		priceChartWidget = binding.priceChart

		super.initView()
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top)
			binding.marketplaceOffersWrapper.updatePadding(bottom = insets.bottom)
		}

		binding.marketplaceViewpager.adapter = MarketplacePagerAdapter(
			fragment = this,
			navigateToFilters = { offerType ->
				findNavController().navigate(
					MarketplaceFragmentDirections.proceedToFiltersFragment(offerType)
				)
			},
			navigateToNewOffer = { offerType ->
				findNavController().navigate(
					MarketplaceFragmentDirections.proceedToNewOfferFragment(offerType)
				)
			},
			navigateToMyOffers = { offerType ->
				findNavController().navigate(
					MarketplaceFragmentDirections.proceedToMyOffersFragment(offerType)
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
		marketplaceViewModel.loadMe()
	}
}