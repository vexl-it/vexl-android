package cz.cleevio.lightspeedskeleton.ui.mainActivity

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
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
import cz.cleevio.core.utils.setExitTransitionGravityStart
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.ActivityMainBinding
import cz.cleevio.network.NetworkError
import cz.cleevio.profile.profileFragment.ProfileFragment
import cz.cleevio.vexl.chat.chatContactList.ChatContactListFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.extensions.showSnackbar
import cz.cleevio.vexl.marketplace.marketplaceFragment.MarketplaceFragment
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

	override fun onResume() {
		super.onResume()

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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		window.setBackgroundDrawableResource(R.color.background)
		// Going to Edge to Edge
		WindowCompat.setDecorFitsSystemWindows(window, false)

		bindObservers()
		setupGroupDeepLinks()
	}

	private fun setupGroupDeepLinks() {
		FirebaseDynamicLinks.getInstance()
			.getDynamicLink(intent)
			.addOnSuccessListener(this) { pendingDynamicLinkData ->
				try {
					val groupCode = pendingDynamicLinkData.link?.lastPathSegment?.toLong() ?: 0L
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

	private fun bindObservers() {
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
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
						}
						NavMainGraphModel.NavGraph.Main -> {
							navController.setGraph(
								R.navigation.nav_bottom_navigation
							)
						}
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
	}

	private fun getFirstFragmentFromNavHost(): Fragment? {
		val navHostFragment = supportFragmentManager.findFragmentById(
			R.id.navHostFragment
		) as NavHostFragment

		return navHostFragment.childFragmentManager.fragments.firstOrNull()
	}

	override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
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
				override fun onAnimationStart(p0: Animator?) {
					binding.bottomNavigation.isVisible = true
				}

				override fun onAnimationEnd(p0: Animator?) {
					binding.bottomNavigation.isVisible = to == bottomInsetValue
				}

				override fun onAnimationCancel(p0: Animator?) {
					binding.bottomNavigation.updateLayoutParams<ViewGroup.MarginLayoutParams> {
						bottomMargin = to
					}
					binding.bottomNavigation.isVisible = to == bottomInsetValue
				}

				override fun onAnimationRepeat(p0: Animator?) = Unit
			})
		}
		bottomBarAnimator?.start()
	}
}
