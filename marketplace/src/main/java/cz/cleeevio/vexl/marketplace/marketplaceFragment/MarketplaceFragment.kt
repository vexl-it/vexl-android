package cz.cleeevio.vexl.marketplace.marketplaceFragment

import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentMarketplaceBinding
import cz.cleeevio.vexl.marketplace.widgets.CurrencyPriceChartViewModel
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.CurrencyPriceChartViewModel
import lightbase.core.baseClasses.BaseFragment
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
		binding.bottomNavigation.setOnItemSelectedListener {

			if (R.id.nav_marketplace == it.itemId) {
				marketplaceViewModel.navMainGraphModel.navigateToGraph(NavMainGraphModel.NavGraph.Marketplace)
			}
			if (R.id.nav_chat == it.itemId) {
				marketplaceViewModel.navMainGraphModel.navigateToGraph(NavMainGraphModel.NavGraph.Chat)
			}
			if (R.id.nav_user == it.itemId) {
				marketplaceViewModel.navMainGraphModel.navigateToGraph(NavMainGraphModel.NavGraph.UserProfile)
			}
//			when(it.itemId) {
//				is R.id.nav_marketplace -> {
//					marketplaceViewModel.navMainGraphModel.navigateToGraph(NavMainGraphModel.NavGraph.Marketplace)
//				}
//				is R.id.nav_chat -> {
//					marketplaceViewModel.navMainGraphModel.navigateToGraph(NavMainGraphModel.NavGraph.Chat)
//				}
//				is R.id.nav_user -> {
//					marketplaceViewModel.navMainGraphModel.navigateToGraph(NavMainGraphModel.NavGraph.UserProfile)
//				}
//			}

			true
		}
	}

}