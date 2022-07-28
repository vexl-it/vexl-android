package cz.cleevio.lightspeedskeleton.ui.mainActivity

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.ActivityMainBinding
import cz.cleevio.network.NetworkError
import cz.cleevio.profile.profileFragment.ProfileFragment
import cz.cleevio.vexl.chat.chatContactList.ChatContactListFragment
import cz.cleevio.vexl.lightbase.core.extensions.showSnackbar
import cz.cleevio.vexl.marketplace.marketplaceFragment.MarketplaceFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

	private lateinit var binding: ActivityMainBinding
	private val viewModel by inject<MainViewModel>()
	private val errorFlow: NetworkError by inject()

	private lateinit var navController: NavController

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		window.setBackgroundDrawableResource(R.color.background)
		// Going to Edge to Edge
		WindowCompat.setDecorFitsSystemWindows(window, false)

		bindObservers()

		val navHostFragment = supportFragmentManager.findFragmentById(
			R.id.navHostFragment
		) as NavHostFragment
		navController = navHostFragment.navController
		navController.addOnDestinationChangedListener(this)

		binding.bottomNavigation.setupWithNavController(navController)
	}

	private fun bindObservers() {
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.navGraphFlow.collect {
					when (it) {
						NavMainGraphModel.NavGraph.EmptyState -> Unit
						NavMainGraphModel.NavGraph.Contacts ->
							navController.setGraph(
								R.navigation.nav_contacts
							)
						NavMainGraphModel.NavGraph.Main ->
							navController.setGraph(
								R.navigation.nav_bottom_navigation
							)
						NavMainGraphModel.NavGraph.Onboarding ->
							navController.setGraph(
								R.navigation.nav_onboarding
							)
						NavMainGraphModel.NavGraph.Marketplace ->
							navController.setGraph(
								R.navigation.nav_marketplace
							)
						NavMainGraphModel.NavGraph.Chat ->
							navController.setGraph(
								R.navigation.nav_chat
							)
						NavMainGraphModel.NavGraph.Profile ->
							navController.setGraph(
								R.navigation.nav_profile
							)
					}
				}
			}
		}

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.encryptedPreferenceRepository.areScreenshotsAllowedFlow.collect {
					if (it) {
						window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
					} else {
						window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
					}
				}
			}
		}

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				errorFlow.error.collect { error ->
					error?.stringMessage?.let { message ->
						showSnackbar(
							view = binding.container,
							message = message
						)
					}

					error?.message?.let { messageCode ->
						if (messageCode != -1) {
							showSnackbar(
								view = binding.container,
								message = getString(messageCode)
							)
						}
					}
				}
			}
		}
	}

	override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
		when (destination.label) {
			MarketplaceFragment::class.java.simpleName,
			ChatContactListFragment::class.java.simpleName,
			ProfileFragment::class.java.simpleName ->
				binding.bottomNavigation.isVisible = true
			else ->
				binding.bottomNavigation.isVisible = false
		}
	}
}