package cz.cleevio.core.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class NavMainGraphModel {

	sealed class NavGraph {

		object EmptyState : NavGraph()

		object Onboarding : NavGraph()
		object Contacts : NavGraph()
		object Marketplace : NavGraph()
		object Profile : NavGraph()
	}

	private val navGraphChannel = Channel<NavGraph>(Channel.RENDEZVOUS)
	val navGraphFlow = navGraphChannel.receiveAsFlow()

	fun navigateToGraph(navGraph: NavGraph) {
		navGraphChannel.trySend(navGraph)
	}

	fun clearChannel() {
		navGraphChannel.trySend(NavGraph.EmptyState)
	}
}