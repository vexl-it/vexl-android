package cz.cleeevio.vexl.marketplace.requestOfferFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel


class RequestOfferViewModel constructor(
	private val offerRepository: OfferRepository
) : BaseViewModel() {

	private val _isRequesting = MutableStateFlow<Boolean>(false)
	val isRequesting = _isRequesting.asStateFlow()

	private val _offer = MutableStateFlow<Offer?>(null)
	val offer = _offer.asStateFlow()

	private val _friends = MutableStateFlow<List<Any>>(listOf())
	val friends = _friends.asStateFlow()

	fun sendRequest(text: String, offerPublicKey: String) {
		viewModelScope.launch(Dispatchers.IO) {
			_isRequesting.emit(true)
			//fake operation todo: connect to BE
			delay(3000)

			_isRequesting.emit(false)
		}
	}

	fun loadOfferById(offerId: String) {
		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.getOffersFlow().collect { offers ->
				_offer.emit(
					offers.firstOrNull { it.offerId == offerId }
				)
			}
		}
	}

	fun loadFriends() {
		viewModelScope.launch(Dispatchers.IO) {
			//somehow load friends? todo: when BE supports this feature
			_friends.emit(
				listOf(
					Any(), Any(), Any()
				)
			)
		}
	}
}