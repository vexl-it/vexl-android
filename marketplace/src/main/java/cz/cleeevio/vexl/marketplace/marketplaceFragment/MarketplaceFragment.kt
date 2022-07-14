package cz.cleeevio.vexl.marketplace.marketplaceFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentMarketplaceBinding
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.CurrencyPriceChartViewModel
import cz.cleevio.repository.repository.chat.ChatRepository
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MarketplaceFragment : BaseFragment(R.layout.fragment_marketplace) {


	private val chatRepository: ChatRepository by inject()

	private val binding by viewBinding(FragmentMarketplaceBinding::bind)
	override val viewModel by viewModel<CurrencyPriceChartViewModel>()
	private val marketplaceViewModel by viewModel<MarketplaceViewModel>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.currentCryptoCurrencyPrice.collect { currentCryptoCurrencyPrice ->
				binding.priceChart.setupCryptoCurrencies(currentCryptoCurrencyPrice)
			}
		}
		repeatScopeOnStart {
			viewModel.marketData.collect { marketData ->
				binding.priceChart.setupMarketData(marketData)
			}
		}
	}

	override fun onResume() {
		super.onResume()
		viewModel.syncMarketData()
	}

	override fun initView() {

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

		repeatScopeOnStart {
			chatRepository.syncAllMessages()
		}

		binding.priceChart.onPriceChartPeriodClicked = {
			viewModel.syncMarketData(it)
		}
	}

}