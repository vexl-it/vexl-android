package cz.cleevio.vexl.marketplace.marketplaceFragment.offers

import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.ChipViewUtils
import cz.cleevio.core.utils.repeatScopeOnResume
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.marketplace.R
import cz.cleevio.vexl.marketplace.databinding.FragmentOffersBinding
import cz.cleevio.vexl.marketplace.marketplaceFragment.NavMarketplaceGraphModel
import org.koin.androidx.viewmodel.ext.android.viewModel

sealed class OffersBaseFragment : BaseFragment(R.layout.fragment_offers) {

	abstract fun getOfferType(): OfferType

	override val viewModel by viewModel<OffersViewModel>()
	protected val binding by viewBinding(FragmentOffersBinding::bind)
	protected val adapter: OffersAdapter by lazy {
		OffersAdapter(requestOffer = {
			viewModel.navigateToGraph(NavMarketplaceGraphModel.NavGraph.RequestOffer(it))
		})
	}

	override fun bindObservers() {
		repeatScopeOnResume {
			viewModel.getFilters(getOfferType()).collect { isFilterInUse ->
				binding.filters.removeAllViews()
				binding.filters.addView(
					ChipViewUtils.generateChipView(
						context = requireContext(),
						icon = R.drawable.ic_chevron_down,
						iconAtStart = false,
						activeState = isFilterInUse,
						filter = getString(R.string.filter_offers),
						listener = {
							viewModel.navigateToGraph(NavMarketplaceGraphModel.NavGraph.Filters(getOfferType()))
						}
					)
				)
			}
		}
		repeatScopeOnResume {
			viewModel.myOffersCount.collect { myOffersCount ->
				processMyOffersButtons(myOffersCount > 0)
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.offersContainer) { insets ->
			binding.offerList.updatePadding(bottom = insets.bottomWithNavBar)
		}

		binding.offerList.adapter = adapter

		binding.addOfferBtn.setOnClickListener {
			viewModel.navigateToGraph(NavMarketplaceGraphModel.NavGraph.NewOffer(getOfferType()))
		}
		binding.myOffersBtn.setOnClickListener {
			viewModel.navigateToGraph(NavMarketplaceGraphModel.NavGraph.MyOffer(getOfferType()))
		}

		checkMyOffersCount(getOfferType())
	}

	private fun processMyOffersButtons(hasMyOffers: Boolean) {
		if (hasMyOffers) {
			binding.addOfferBtn.isVisible = false
			binding.myOffersBtn.isVisible = true
		} else {
			binding.addOfferBtn.isVisible = true
			binding.myOffersBtn.isVisible = false
		}
	}

	protected fun checkMyOffersCount(offerType: OfferType) {
		viewModel.checkMyOffersCount(offerType)
	}
}