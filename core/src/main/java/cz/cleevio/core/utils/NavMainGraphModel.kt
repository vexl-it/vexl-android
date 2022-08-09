package cz.cleevio.core.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber

class NavMainGraphModel {

	sealed class NavGraph {

		object EmptyState : NavGraph()

		object Onboarding : NavGraph()
		object Contacts : NavGraph()
		object Main : NavGraph()
		object Marketplace : NavGraph()
		object Chat : NavGraph()
		object Profile : NavGraph()
	}

	private val navGraphChannel = Channel<NavGraph>(Channel.RENDEZVOUS)
	val navGraphFlow = navGraphChannel.receiveAsFlow()

	suspend fun navigateToGraph(navGraph: NavGraph) {
		navGraphChannel.send(navGraph)
		Timber.d("Graph: ${navGraph}")
	}

	fun clearChannel() {
		navGraphChannel.trySend(NavGraph.EmptyState)
	}
}