package cz.cleevio.core.utils

import cz.cleevio.core.model.OfferType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber

class NavMainGraphModel {

	sealed class NavGraph {

		object EmptyState : NavGraph()

		object Onboarding : NavGraph()
		object OnboardingIdentity : NavGraph()
		object Contacts : NavGraph()
		object Main : NavGraph()
		object Intro : NavGraph()
		object ForceUpdate : NavGraph()
		object Maintenance : NavGraph()
		object ForceNotificationPermission : NavGraph()
		object Marketplace : NavGraph()
		object Chat : NavGraph()
		object Profile : NavGraph()
		data class ChatDetail(val inboxKey: String? = null, val senderKey: String? = null) : NavGraph()
		object ChatRequests : NavGraph()
		object ChatList : NavGraph()
		data class MyOfferList(val offerType: OfferType) : NavGraph()
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