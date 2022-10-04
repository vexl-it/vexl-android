package cz.cleevio.vexl.marketplace.reportOffer

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
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

class ReportOfferConfirmViewModel constructor(
	private val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	fun navigateToMarketPlace() {
		viewModelScope.launch(Dispatchers.Default) {
			navMainGraphModel.navigateToGraph(
				NavMainGraphModel.NavGraph.Marketplace
			)
		}
	}
}