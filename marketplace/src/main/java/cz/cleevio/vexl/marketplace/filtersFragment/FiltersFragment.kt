package cz.cleevio.vexl.marketplace.filtersFragment

import android.content.res.Resources
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.TransitionManager
import cz.cleevio.core.base.BaseGraphFragment
import cz.cleevio.core.utils.getBitmap
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.*
import cz.cleevio.repository.model.Currency
import cz.cleevio.repository.model.offer.OfferFilter
import cz.cleevio.vexl.lightbase.core.extensions.dpValueToPx
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.marketplace.LocationSuggestionAdapter
import cz.cleevio.vexl.marketplace.R
import cz.cleevio.vexl.marketplace.SelectGroupAdapter
import cz.cleevio.vexl.marketplace.databinding.FragmentFiltersBinding
import cz.cleevio.vexl.marketplace.editOfferFragment.NUMBER_OF_COLUMNS
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class FiltersFragment : BaseGraphFragment(R.layout.fragment_filters) {

	private val binding by viewBinding(FragmentFiltersBinding::bind)
	private val args by navArgs<FiltersFragmentArgs>()
	lateinit var adapter: SelectGroupAdapter

	private val filterViewModel by viewModel<FiltersViewModel> {
		parametersOf(args.offerType)
	}

	override var priceChartWidget: CurrencyPriceChartWidget? = null

	override fun initView() {
		adapter = SelectGroupAdapter()
		val layoutManager = GridLayoutManager(requireContext(), NUMBER_OF_COLUMNS)
		binding.groupRecycler.layoutManager = layoutManager
		binding.groupRecycler.adapter = adapter

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
		binding.filterLocation.setFragmentManager(parentFragmentManager)

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

		binding.friendLevel.isMultichoiceEnabled(true)
		binding.friendLevel.resetFromFilters()

		binding.applyBtn.setOnClickListener {
			filterViewModel.saveOfferFilter(
				locationTypes = binding.filterLocation.getSelectedLocationTypes(),
				locationValues = binding.filterLocation.getLocationValues(),
				paymentMethod = binding.paymentMethod.getPaymentValue(),
				btcNetwork = binding.networkType.getBtcNetworkValue(),
				friendLevels = binding.friendLevel.getMultichoiceFriendLevels(),
				feeValue = if (binding.filterOfferFee.isVisible) {
					binding.filterOfferFee.getFeeValue()
				} else {
					null
				},
				feeTypes = binding.filterOfferFee.getFeeTypes(),
				priceRange = if (binding.priceRangeWidget.isVisible) {
					binding.priceRangeWidget.getPriceRangeValue()
				} else {
					null
				},
				currency = if (binding.priceRangeWidget.isVisible) {
					binding.priceRangeWidget.getCurrencyName()
				} else {
					null
				},
				groupUuids = adapter.getSelectedGroupUuids()
			)
			findNavController().popBackStack()
		}
	}

	private fun resetAllInputs() {
		binding.newOfferCurrency.reset()
		binding.priceRangeWidget.reset()
		binding.filterOfferFee.reset()
		binding.filterLocation.reset()
		binding.paymentMethod.reset()
		binding.networkType.reset()
		binding.friendLevel.resetFromFilters()
		binding.filterOfferFee.isVisible = false
		binding.amountTitle.isVisible = false
		binding.priceRangeWidget.isVisible = false
		adapter.setSelectedGroupUuids(emptyList())

		TransitionManager.beginDelayedTransition(binding.container)
	}

	override fun bindObservers() {
		super.bindObservers()

		repeatScopeOnStart {
			filterViewModel.userFlow.collect {
				it?.let { user ->
					binding.friendLevel.setUserAvatar(user.avatarBase64?.getBitmap(), user.anonymousAvatarImageIndex)
				}
			}
		}

		repeatScopeOnStart {
			filterViewModel.suggestions.collect { (offerLocationItem, queries) ->
				if (queries.isEmpty()) return@collect
				if (queries.map { it.cityText }.contains(offerLocationItem?.getEditText()?.text.toString())) {
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
		repeatScopeOnStart {
			filterViewModel.setupViewFlow.collect { offerFilter ->
				setupFilterViews(offerFilter)
			}
		}
		repeatScopeOnStart {
			filterViewModel.groups.collect { groups ->
				adapter.submitList(groups)
				binding.groupTitle.isVisible = groups.isNotEmpty()
				binding.groupDescription.isVisible = groups.isNotEmpty()
			}
		}
	}

	private fun setupFilterViews(offerFilter: OfferFilter) {
		binding.filterLocation.reset()
		binding.filterLocation.setValues(
			offerFilter.locations ?: emptyList(),
			offerFilter.locationTypes?.map { LocationButtonSelected.valueOf(it) } ?: emptyList()
		)
		offerFilter.paymentMethods?.let { methods ->
			binding.paymentMethod.setValues(
				methods.map { PaymentButtonSelected.valueOf(it) }
			)
		}
		offerFilter.btcNetworks?.let { networks ->
			binding.networkType.setValues(
				networks.map { BtcNetworkButtonSelected.valueOf(it) }
			)
		}
		offerFilter.friendLevels?.let { levels ->
			binding.friendLevel.setValues(levels.map { FriendLevel.valueOf(it) }.toSet())
		}
		binding.filterOfferFee.setValues(
			value = offerFilter.feeValue,
			selected = offerFilter.feeTypes?.map { FeeButtonSelected.valueOf(it) } ?: emptyList()
		)
		binding.filterOfferFee.isVisible = offerFilter.feeTypes?.isEmpty() == false

		offerFilter.currency?.let {
			val currency = Currency.valueOf(it)
			binding.priceRangeWidget.setupWithCurrency(currency)
			binding.newOfferCurrency.selectCurrencyManually(currency)
		}
		offerFilter.priceRangeTopLimit?.let { topLimit ->
			offerFilter.priceRangeBottomLimit?.let { bottomLimit ->
				binding.priceRangeWidget.setValues(
					bottomLimit = bottomLimit,
					topLimit = topLimit
				)
				binding.amountTitle.isVisible = true
				binding.priceRangeWidget.isVisible = true
				binding.filterOfferFee.isVisible = true
			}
		}
		if (!offerFilter.groupUuids.isNullOrEmpty()) {
			adapter.setSelectedGroupUuids(offerFilter.groupUuids ?: emptyList())
		}
	}

	private companion object {
		private const val DISPLAY_THIRD = 3
		private const val SUGGESTION_PADDING = 8
		private const val OFFER_ITEM_PADDING = 60
	}
}
