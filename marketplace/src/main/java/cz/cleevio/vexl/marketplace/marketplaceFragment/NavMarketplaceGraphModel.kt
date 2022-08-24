package cz.cleevio.vexl.marketplace.marketplaceFragment

import cz.cleevio.core.model.OfferType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber

class NavMarketplaceGraphModel {

	sealed class NavGraph {

		object EmptyState : NavGraph()
		data class Filters(val type: OfferType) : NavGraph()
		data class NewOffer(val type: OfferType) : NavGraph()
		data class MyOffer(val type: OfferType) : NavGraph()
		data class RequestOffer(val offerId: String) : NavGraph()
	}

	private val navGraphChannel = Channel<NavGraph>(Channel.RENDEZVOUS)
	val navGraphFlow = navGraphChannel.receiveAsFlow()

	suspend fun navigateToGraph(navGraph: NavGraph) {
		navGraphChannel.send(navGraph)
		Timber.d("Graph: $navGraph")
	}

	fun clearChannel() {
		navGraphChannel.trySend(NavGraph.EmptyState)
	}
}