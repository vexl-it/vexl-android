package cz.cleevio.vexl.marketplace.marketplaceFragment

import android.os.Bundle
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.material.tabs.TabLayoutMediator
import cz.cleevio.core.base.BaseGraphFragment
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.setEnterTransitionZSharedAxis
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.CurrencyPriceChartWidget
import cz.cleevio.core.widget.JoinGroupBottomSheetDialog
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.marketplace.R
import cz.cleevio.vexl.marketplace.databinding.FragmentMarketplaceBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MarketplaceFragment : BaseGraphFragment(R.layout.fragment_marketplace) {

	private val binding by viewBinding(FragmentMarketplaceBinding::bind)
	private val marketplaceViewModel by viewModel<MarketplaceViewModel>()

	override var priceChartWidget: CurrencyPriceChartWidget? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setEnterTransitionZSharedAxis()
	}

	override fun onResume() {
		super.onResume()
		viewModel.syncMarketData()
	}

	override fun onStart() {
		super.onStart()

		marketplaceViewModel.checkAndLoadGroupFromDeeplink()
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

		binding.swipeRefresh.setOnRefreshListener {
			marketplaceViewModel.syncOffers()
			marketplaceViewModel.loadMe()
			binding.swipeRefresh.isRefreshing = false
		}

		binding.marketplaceViewpager.adapter = MarketplacePagerAdapter(this)

		priceChartWidget?.onLayoutChanged = {
			TransitionManager.beginDelayedTransition(binding.container)
		}

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

	override fun bindObservers() {
		super.bindObservers()

		repeatScopeOnStart {
			marketplaceViewModel.groupLoaded.collect { group ->
				showBottomDialog(
					JoinGroupBottomSheetDialog(
						groupName = group.name,
						groupLogo = group.logoUrl ?: "",
						groupCode = group.code,
						isFromDeeplink = true
					)
				)
			}
		}

		repeatScopeOnStart {
			marketplaceViewModel.navMarketplaceGraphFlow.collect {
				when (it) {
					is NavMarketplaceGraphModel.NavGraph.Filters -> {
						findNavController().navigate(
							MarketplaceFragmentDirections.proceedToFiltersFragment(it.type)
						)
					}
					is NavMarketplaceGraphModel.NavGraph.MyOffer -> {
						findNavController().safeNavigateWithTransition(
							MarketplaceFragmentDirections.proceedToMyOffersFragment(it.type)
						)
					}
					is NavMarketplaceGraphModel.NavGraph.NewOffer -> {
						findNavController().safeNavigateWithTransition(
							MarketplaceFragmentDirections.proceedToNewOfferFragment(it.type)
						)
					}
					is NavMarketplaceGraphModel.NavGraph.RequestOffer -> {
						findNavController().safeNavigateWithTransition(
							MarketplaceFragmentDirections.proceedToRequestOfferFragment(it.offerId)
						)
					}
				}
			}
		}
	}
}
