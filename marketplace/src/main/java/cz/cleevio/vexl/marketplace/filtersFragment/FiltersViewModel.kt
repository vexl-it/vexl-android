package cz.cleevio.vexl.marketplace.filtersFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.model.*
import cz.cleevio.core.widget.FeeButtonSelected
import cz.cleevio.core.widget.FriendLevel
import cz.cleevio.core.widget.LocationButtonSelected
import cz.cleevio.core.widget.OfferLocationItem
import cz.cleevio.repository.model.offer.Location
import cz.cleevio.repository.model.offer.LocationSuggestion
import cz.cleevio.repository.model.offer.OfferFilter
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class FiltersViewModel constructor(
	private val offerType: OfferType,
	private val offerRepository: OfferRepository,
	private val groupRepository: GroupRepository
) : BaseViewModel() {

	private val _queryForSuggestions =
		MutableStateFlow<Pair<OfferLocationItem?, String>>(Pair(null, ""))

	val groups = groupRepository.getGroupsFlow()

	@OptIn(FlowPreview::class)
	val queryForSuggestions = _queryForSuggestions.asStateFlow()
		.debounce(DEBOUNCE)

	private val _suggestions: MutableStateFlow<Pair<OfferLocationItem?, List<LocationSuggestion>>> =
		MutableStateFlow(Pair(null, listOf()))
	val suggestions = _suggestions.asStateFlow()

	private val _setupViewChannel = Channel<OfferFilter>()
	val setupViewFlow = _setupViewChannel.receiveAsFlow()

	init {
		viewModelScope.launch(Dispatchers.Default) {
			_setupViewChannel.send(
				if (offerType.isBuy()) offerRepository.buyOfferFilter.value
				else offerRepository.sellOfferFilter.value
			)
		}
	}

	fun getSuggestions(query: String, viewReference: OfferLocationItem) {
		viewModelScope.launch(Dispatchers.IO) {
			_queryForSuggestions.emit(Pair(viewReference, query))
		}
	}

	fun getDebouncedSuggestions(query: String, viewReference: OfferLocationItem) {
		viewModelScope.launch(Dispatchers.IO) {
			val result = offerRepository.getLocationSuggestions(
				count = SUGGESTION_COUNT,
				query = query,
				language = SUGGESTION_LANGUAGES
			)
			if (result.isSuccess()) {
				_suggestions.emit(Pair(viewReference, result.data.orEmpty()))
			}
		}
	}

	fun saveOfferFilter(
		locationTypes: Set<LocationButtonSelected>,
		locationValues: List<Location>,
		paymentMethod: PaymentMethodValue,
		btcNetwork: BtcNetworkValue,
		friendLevels: Set<FriendLevel>,
		feeValue: Float?,
		feeTypes: Set<FeeButtonSelected>,
		priceRange: PriceRangeValue?,
		currency: String?,
		groupUuids: List<String>
	) {
		val offerFilter = OfferFilter(
			locationTypes = locationTypes.map { it.name }.toSet(),
			locations = locationValues,
			paymentMethods = paymentMethod.value.map { it.name }.toSet(),
			btcNetworks = btcNetwork.value.map { it.name }.toSet(),
			friendLevels = friendLevels.map { it.name }.toSet(),
			feeTypes = feeTypes.map { it.name }.toSet(),
			feeValue = if(FeeButtonSelected.WITH_FEE in feeTypes) feeValue else null,
			priceRangeTopLimit = priceRange?.topLimit,
			priceRangeBottomLimit = priceRange?.bottomLimit,
			currency = currency,
			groupUuids = groupUuids
		)
		if (offerType.isBuy()) {
			offerRepository.buyOfferFilter.value = offerFilter
		} else {
			offerRepository.sellOfferFilter.value = offerFilter
		}
	}

	private companion object {
		private const val DEBOUNCE = 300L
		private const val SUGGESTION_COUNT = 20
		private const val SUGGESTION_LANGUAGES = "cs,en"
	}
}
