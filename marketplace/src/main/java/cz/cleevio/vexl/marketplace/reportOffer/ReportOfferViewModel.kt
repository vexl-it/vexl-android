package cz.cleevio.vexl.marketplace.reportOffer

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportOfferViewModel constructor(
	private val offerId: String,
	private val offerRepository: OfferRepository
) : BaseViewModel() {

	private val _offerRequest = MutableSharedFlow<Resource<Unit>>()
	val offerRequest = _offerRequest.asSharedFlow()

	fun reportOffer() {
		viewModelScope.launch(Dispatchers.IO) {
			_offerRequest.emit(offerRepository.reportOffer(offerId))
		}
	}
}