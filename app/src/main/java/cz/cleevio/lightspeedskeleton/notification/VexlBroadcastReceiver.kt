package cz.cleevio.lightspeedskeleton.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cz.cleevio.core.utils.NavMainGraphModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VexlBroadcastReceiver : BroadcastReceiver(), KoinComponent {

	private val navGraphModel by inject<NavMainGraphModel>()

	override fun onReceive(context: Context?, intent: Intent?) {
		navGraphModel.navigateToGraph(NavMainGraphModel.NavGraph.Main)
	}
}