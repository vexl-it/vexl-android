package cz.cleevio.vexl.marketplace.filtersFragment

import android.content.res.Resources
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionManager
import cz.cleevio.core.base.BaseGraphFragment
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.CurrencyPriceChartWidget
import cz.cleevio.vexl.lightbase.core.extensions.dpValueToPx
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.marketplace.LocationSuggestionAdapter
import cz.cleevio.vexl.marketplace.R
import cz.cleevio.vexl.marketplace.databinding.FragmentFiltersBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class FiltersFragment : BaseGraphFragment(R.layout.fragment_filters) {

	private val binding by viewBinding(FragmentFiltersBinding::bind)
	private val args by navArgs<FiltersFragmentArgs>()
	private val filterViewModel by viewModel<FiltersViewModel>()

	override var priceChartWidget: CurrencyPriceChartWidget? = null

	override fun initView() {
		priceChartWidget = binding.priceChart

		super.initView()
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME
			)
		}

		binding.filterTitleWidget.setTypeAndTitle(args.offerType, getString(R.string.filter_title))
		binding.filterTitleWidget.setListeners(
			onReset = {
				//reset all fields
				resetAllInputs()
			},
			onClose = {
				//close this screen
				findNavController().popBackStack()
			}
		)

		binding.filterLocation.setupFocusChangeListener { hasFocus, locationItem ->
			if (hasFocus) {
				binding.nestedScrollView.smoothScrollTo(
					binding.filterLocation.x.toInt(),
					locationItem.height *
						binding.filterLocation.getPositionOfItem(locationItem) +
						requireContext().dpValueToPx(OFFER_ITEM_PADDING).toInt() +
						binding.filterLocation.y.toInt() -
						Resources.getSystem().displayMetrics.heightPixels / DISPLAY_THIRD
				)
			}
		}

		binding.filterLocation.setupOnTextChanged { query, view ->
			filterViewModel.getSuggestions(query, view)
		}

		binding.newOfferCurrency.onCurrencyPicked = {
			binding.priceRangeWidget.setupWithCurrency(it)
			binding.filterOfferFee.isVisible = true
			binding.amountTitle.isVisible = true
			binding.priceRangeWidget.isVisible = true
			TransitionManager.beginDelayedTransition(binding.container)
		}

		binding.newOfferCurrency.selectCurrencyManually(null)

		priceChartWidget?.onLayoutChanged = {
			TransitionManager.beginDelayedTransition(binding.container)
		}

		binding.applyBtn.setOnClickListener {
			//TODO: somehow apply filters? Save to DB?

			//and go back to offers
			findNavController().popBackStack()
		}

		binding.filterLocation.setFragmentManager(parentFragmentManager)
	}

	private fun resetAllInputs() {
		binding.newOfferCurrency.reset()
		binding.priceRangeWidget.reset()
		binding.filterOfferFee.reset()
		binding.filterLocation.reset()
		binding.paymentMethod.reset()
		binding.networkType.reset()
		binding.friendLevel.reset()
		binding.filterOfferFee.isVisible = false
		binding.amountTitle.isVisible = false
		binding.priceRangeWidget.isVisible = false

		TransitionManager.beginDelayedTransition(binding.container)
	}

	override fun bindObservers() {
		super.bindObservers()

		repeatScopeOnStart {
			filterViewModel.suggestions.collect { (offerLocationItem, queries) ->
				if (queries.isEmpty()) return@collect
				if (queries.map { it.city }.contains(offerLocationItem?.getEditText()?.text.toString())) {
					offerLocationItem?.setLocation(queries.first())
					return@collect
				}

				offerLocationItem?.getEditText()?.let {
					it.setAdapter(null)
					val adapter = LocationSuggestionAdapter(queries, requireActivity())

					it.dropDownVerticalOffset = requireContext().dpValueToPx(SUGGESTION_PADDING).toInt()
					it.setDropDownBackgroundResource(R.drawable.background_rounded)
					it.setAdapter(adapter)
					it.showDropDown()
					it.setOnItemClickListener { _, _, position, _ ->
						offerLocationItem.setLocation(queries[position])
					}
				}
			}
		}

		repeatScopeOnStart {
			filterViewModel.queryForSuggestions.collect { (view, query) ->
				view?.let {
					filterViewModel.getDebouncedSuggestions(query, it)
				}
			}
		}
	}

	private companion object {
		private const val DISPLAY_THIRD = 3
		private const val SUGGESTION_PADDING = 8
		private const val OFFER_ITEM_PADDING = 60
	}
}
