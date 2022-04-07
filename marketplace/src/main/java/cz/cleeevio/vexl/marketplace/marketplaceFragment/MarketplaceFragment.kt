package cz.cleeevio.vexl.marketplace.marketplaceFragment

import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentMarketplaceBinding
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
	}

}