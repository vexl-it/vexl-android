package cz.cleevio.lightspeedskeleton.ui.mainActivity

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.setExitTransitionGravityStart
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.ActivityMainBinding
import cz.cleevio.lightspeedskeleton.notification.RemoteNotificationType
import cz.cleevio.lightspeedskeleton.notification.VexlFirebaseMessagingService
import cz.cleevio.lightspeedskeleton.ui.checkForMaintenance
import cz.cleevio.lightspeedskeleton.ui.checkForUpdate
import cz.cleevio.network.NetworkError
import cz.cleevio.profile.profileFragment.ProfileFragment
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.vexl.chat.chatContactList.ChatContactListFragment
import cz.cleevio.vexl.chat.chatContactList.ChatContactListFragmentDirections
import cz.cleevio.vexl.lightbase.core.extensions.isNotNullOrBlank
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.extensions.showSnackbar
import cz.cleevio.vexl.marketplace.marketplaceFragment.MarketplaceFragment
import cz.cleevio.vexl.marketplace.marketplaceFragment.MarketplaceFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

	private lateinit var binding: ActivityMainBinding
	private val viewModel by inject<MainViewModel>()
	private val errorFlow: NetworkError by inject()

	private lateinit var navController: NavController
	private var bottomBarAnimator: ValueAnimator? = null
	private var bottomInsetValue = 0
	private var lastVisitedGraph: Int? = null
	private var graphSetInOnCreate: Boolean = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		Timber.tag("REENCRYPT_MIGRATION").d("MainActivity onCreate")

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		window.setBackgroundDrawableResource(R.color.background)
		// Going to Edge to Edge
		WindowCompat.setDecorFitsSystemWindows(window, false)

		setupBottomNavigationBar()
		bindObservers()
		setupGroupDeepLinks()

		setLastVisitedGraph(savedInstanceState)
		graphSetInOnCreate = true

		resolveNotificationIntent(this.intent)

		if (!viewModel.encryptedPreferenceRepository.hasConvertedAvatarToSmallerSize) {
			viewModel.convertAvatarToSmallerSize()
		}

		if (!viewModel.encryptedPreferenceRepository.hasCreatedInternalInboxesForOffers) {
			viewModel.createInboxesForOffers()
		}
	}

	override fun onNewIntent(intent: android.content.Intent?) {
		super.onNewIntent(intent)

		resolveNotificationIntent(intent)
	}

	private fun resolveNotificationIntent(intent: android.content.Intent?) {
		intent?.let { intent ->
			lifecycleScope.launch {
				val type = RemoteNotificationType.valueOf(
					intent.extras?.getString(VexlFirebaseMessagingService.NOTIFICATION_TYPE, RemoteNotificationType.UNKNOWN.name)
						?: RemoteNotificationType.UNKNOWN.name
				)
				val inboxKey = intent.extras?.getString(VexlFirebaseMessagingService.NOTIFICATION_INBOX) ?: ""
				val senderKey = intent.extras?.getString(VexlFirebaseMessagingService.NOTIFICATION_SENDER) ?: ""

				when (type) {
					RemoteNotificationType.MESSAGE,
					RemoteNotificationType.REQUEST_REVEAL,
					RemoteNotificationType.APPROVE_REVEAL,
					RemoteNotificationType.DISAPPROVE_REVEAL,
					RemoteNotificationType.APPROVE_MESSAGING -> {
						viewModel.navMainGraphModel.navigateToGraph(
							NavMainGraphModel.NavGraph.ChatDetail(inboxKey = inboxKey, senderKey = senderKey)
						)
					}
					RemoteNotificationType.REQUEST_MESSAGING,
					RemoteNotificationType.DISAPPROVE_MESSAGING -> {
						viewModel.navMainGraphModel.navigateToGraph(
							NavMainGraphModel.NavGraph.ChatRequests
						)
					}
					RemoteNotificationType.DELETE_CHAT -> {
						viewModel.navMainGraphModel.navigateToGraph(
							NavMainGraphModel.NavGraph.ChatList
						)
					}
					else -> {
						//do nothing
					}
				}
			}
		}
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)

		if (!graphSetInOnCreate) {
			setLastVisitedGraph(savedInstanceState)
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putBoolean(ACTIVITY_STARTED_BEFORE, true)
		outState.putBundle(GRAPH_STATE, navController.saveState())

		lastVisitedGraph?.let {
			outState.putInt(LAST_VISITED_GRAPH, it)
		}
	}

	override fun onResume() {
		super.onResume()

		//reset flag
		graphSetInOnCreate = false
	}

	private fun setupBottomNavigationBar() {
		val navHostFragment = supportFragmentManager.findFragmentById(
			R.id.navHostFragment
		) as NavHostFragment

		navController = navHostFragment.navController
		navController.addOnDestinationChangedListener(this)

		binding.bottomNavigation.setOnApplyWindowInsetsListener(null)

		listenForInsets(binding.container) { insets ->
			bottomInsetValue = insets.bottom
		}

		binding.bottomNavigation.setupWithNavController(navController)
	}

	private fun setupGroupDeepLinks() {
		FirebaseDynamicLinks.getInstance()
			.getDynamicLink(intent)
			.addOnSuccessListener(this) { pendingDynamicLinkData ->
				try {
					val groupCode = pendingDynamicLinkData?.link?.lastPathSegment?.toLong() ?: 0L
					viewModel.encryptedPreferenceRepository.groupCode = groupCode
				} catch (ex: NumberFormatException) {
					Timber.e(ex)
					FirebaseCrashlytics.getInstance().recordException(
						IllegalStateException("${pendingDynamicLinkData.link?.lastPathSegment} is not valid Long")
					)
				}
			}
			.addOnFailureListener(this) { exception ->
				Timber.e(exception)
				FirebaseCrashlytics.getInstance().recordException(IllegalStateException("Dynamic link failure"))
			}
	}

	@Suppress("ComplexMethod", "LongMethod")
	private fun bindObservers() {
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.CREATED) {
				viewModel.navGraphFlow.collect {
					when (it) {
						NavMainGraphModel.NavGraph.EmptyState -> Unit
						NavMainGraphModel.NavGraph.Contacts -> {
							getFirstFragmentFromNavHost()?.apply {
								this.setExitTransitionGravityStart()
							}
							navController.setGraph(
								R.navigation.nav_contacts
							)
							lastVisitedGraph = R.navigation.nav_contacts
						}
						NavMainGraphModel.NavGraph.Main -> {
							if (viewModel.firstTimeMainScreen) {
								checkNotifications()
							} else {
								navController.setGraph(
									R.navigation.nav_bottom_navigation
								)
								lastVisitedGraph = R.navigation.nav_bottom_navigation
							}
						}
						NavMainGraphModel.NavGraph.Intro -> {
							navController.setGraph(
								R.navigation.nav_intro
							)
							lastVisitedGraph = R.navigation.nav_intro
						}
						NavMainGraphModel.NavGraph.ForceUpdate -> {
							navController.setGraph(
								R.navigation.nav_force_update
							)
							lastVisitedGraph = R.navigation.nav_force_update
						}
						NavMainGraphModel.NavGraph.Maintenance -> {
							navController.setGraph(
								R.navigation.nav_maintenance
							)
							lastVisitedGraph = R.navigation.nav_maintenance
						}
						NavMainGraphModel.NavGraph.ForceNotificationPermission -> {
							navController.setGraph(
								R.navigation.nav_force_notification_permission
							)
							lastVisitedGraph = R.navigation.nav_force_notification_permission
						}
						NavMainGraphModel.NavGraph.Onboarding -> {
							navController.setGraph(
								R.navigation.nav_onboarding
							)
							lastVisitedGraph = R.navigation.nav_onboarding
						}
						NavMainGraphModel.NavGraph.OnboardingIdentity -> {
							getFirstFragmentFromNavHost()?.apply {
								this.setExitTransitionGravityStart()
							}
							navController.setGraph(
								R.navigation.nav_onboarding_identity
							)
							lastVisitedGraph = R.navigation.nav_onboarding_identity
						}
						NavMainGraphModel.NavGraph.Marketplace -> {
							navController.setGraph(
								R.navigation.nav_marketplace
							)
							lastVisitedGraph = R.navigation.nav_marketplace
						}
						NavMainGraphModel.NavGraph.Chat -> {
							navController.setGraph(
								R.navigation.nav_chat
							)
							lastVisitedGraph = R.navigation.nav_chat
						}
						NavMainGraphModel.NavGraph.Profile -> {
							navController.setGraph(
								R.navigation.nav_profile
							)
							lastVisitedGraph = R.navigation.nav_profile
						}

						is NavMainGraphModel.NavGraph.ChatDetail -> {
							binding.bottomNavigation.post {
								if (it.inboxKey.isNotNullOrBlank() && it.senderKey.isNotNullOrBlank()) {
									//TODO: hack that should be fixed later
									binding.bottomNavigation.findViewById<View>(R.id.nav_chat)?.performClick()

									viewModel.goToChatDetail(
										inboxKey = it.inboxKey ?: "",
										senderKey = it.senderKey ?: ""
									)
								}
							}
						}

						NavMainGraphModel.NavGraph.ChatRequests -> {
							binding.bottomNavigation.post {
								//TODO: hack that should be fixed later
								binding.bottomNavigation.findViewById<View>(R.id.nav_chat)?.performClick()

								navController.safeNavigateWithTransition(
									ChatContactListFragmentDirections.proceedToChatRequestFragment()
								)
							}
						}

						NavMainGraphModel.NavGraph.ChatList -> {
							//TODO: hack that should be fixed later
							binding.bottomNavigation.post {
								binding.bottomNavigation.findViewById<View>(R.id.nav_chat)?.performClick()
							}
						}

						is NavMainGraphModel.NavGraph.MyOfferList -> {
							binding.bottomNavigation.post {
								binding.bottomNavigation.findViewById<View>(R.id.nav_marketplace)?.performClick()
								navController.safeNavigateWithTransition(
									MarketplaceFragmentDirections.proceedToMyOffersFragment(it.offerType)
								)
							}
						}
					}
				}
			}
		}

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.navigateToChatDetail.collect { userWithMessage ->
					navController.safeNavigateWithTransition(
						ChatContactListFragmentDirections.proceedToChatFragment(
							communicationRequest = CommunicationRequest(
								message = userWithMessage.message,
								offer = userWithMessage.offer
							)
						)
					)
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
			repeatOnLifecycle(Lifecycle.State.RESUMED) {
				viewModel.bottomBarAnimation.collect {
					withContext(Dispatchers.Main) {
						setBottomBarVisibility(it)
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

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				errorFlow.logout.collect { error ->
					viewModel.logout(
						onSuccess = { triggerAppRestart() },
						onError = { triggerAppRestart() }
					)
				}
			}
		}

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.RESUMED) {
				if (checkForUpdate(viewModel.remoteConfig)) {
					viewModel.navMainGraphModel.navigateToGraph(
						NavMainGraphModel.NavGraph.ForceUpdate
					)
				} else if (checkForMaintenance(viewModel.remoteConfig)) {
					viewModel.navMainGraphModel.navigateToGraph(
						NavMainGraphModel.NavGraph.Maintenance
					)
				}
			}
		}
	}

	private fun checkNotifications() {
		lifecycleScope.launch {
			viewModel.firstTimeMainScreen = false
			if (viewModel.notificationUtils.areNotificationsDisabled()) {
				viewModel.navMainGraphModel.navigateToGraph(
					NavMainGraphModel.NavGraph.ForceNotificationPermission
				)
			} else {
				viewModel.navMainGraphModel.navigateToGraph(
					NavMainGraphModel.NavGraph.Main
				)
			}
		}
	}

	private fun triggerAppRestart() {
		lifecycleScope.launch {
			viewModel.navMainGraphModel.navigateToGraph(
				NavMainGraphModel.NavGraph.Onboarding
			)
		}
	}

	private fun getFirstFragmentFromNavHost(): Fragment? {
		val navHostFragment = supportFragmentManager.findFragmentById(
			R.id.navHostFragment
		) as NavHostFragment

		return navHostFragment.childFragmentManager.fragments.firstOrNull()
	}

	override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
		FirebaseCrashlytics.getInstance().log("Destination: ${destination.label}")
		when (destination.label) {
			MarketplaceFragment::class.java.simpleName,
			ChatContactListFragment::class.java.simpleName,
			ProfileFragment::class.java.simpleName ->
				viewModel.setBottomBarState(true)
			else ->
				viewModel.setBottomBarState(false)
		}
	}

	private fun setBottomBarVisibility(visible: Boolean) {
		val height = binding.bottomNavigation.height
		if (visible) {
			if (binding.bottomNavigation.marginBottom == bottomInsetValue) return
			startAnimator(-height, bottomInsetValue)
		} else {
			if (binding.bottomNavigation.marginBottom < bottomInsetValue) return
			startAnimator(bottomInsetValue, -height)
		}
	}

	private fun startAnimator(from: Int, to: Int) {
		bottomBarAnimator?.cancel()
		bottomBarAnimator = ValueAnimator.ofInt(from, to).apply {
			duration = resources.getInteger(R.integer.bottom_bar_animation_duration).toLong()
			addUpdateListener {
				binding.bottomNavigation.updateLayoutParams<ViewGroup.MarginLayoutParams> {
					bottomMargin = it.animatedValue as Int
				}
			}
			addListener(object : Animator.AnimatorListener {
				override fun onAnimationStart(p0: Animator) {
					binding.bottomNavigation.isVisible = true
				}

				override fun onAnimationEnd(p0: Animator) {
					binding.bottomNavigation.isVisible = to == bottomInsetValue
				}

				override fun onAnimationCancel(p0: Animator) {
					binding.bottomNavigation.updateLayoutParams<ViewGroup.MarginLayoutParams> {
						bottomMargin = to
					}
					binding.bottomNavigation.isVisible = to == bottomInsetValue
				}

				override fun onAnimationRepeat(p0: Animator) = Unit
			})
		}
		bottomBarAnimator?.start()
	}

	private fun setLastVisitedGraph(savedInstanceState: Bundle?) {
		lastVisitedGraph = savedInstanceState?.getInt(LAST_VISITED_GRAPH, R.navigation.nav_main)

		val startedBefore = savedInstanceState?.getBoolean(ACTIVITY_STARTED_BEFORE) ?: false
		if (startedBefore) {
			navController.restoreState(savedInstanceState?.getBundle(GRAPH_STATE))
			navController.setGraph(lastVisitedGraph ?: R.navigation.nav_main)
		} else {
			navController.setGraph(R.navigation.nav_main)
		}
	}

	companion object {
		private const val ACTIVITY_STARTED_BEFORE = "activity_started_before"
		private const val LAST_VISITED_GRAPH = "last_visited_graph"
		private const val GRAPH_STATE = "LAST_VISITED_GRAPH"
	}
}
