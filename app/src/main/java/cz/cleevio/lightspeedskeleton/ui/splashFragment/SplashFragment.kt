package cz.cleevio.lightspeedskeleton.ui.splashFragment

import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import cz.cleevio.core.utils.*
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.FragmentSplashBinding
import cz.cleevio.network.data.Status
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.marketplace.encryptingProgressFragment.EncryptingProgressBottomSheetDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val SPLASH_DELAY = 500L

class SplashFragment : BaseFragment(R.layout.fragment_splash) {

	private val binding by viewBinding(FragmentSplashBinding::bind)
	override val viewModel by viewModel<SplashViewModel>()

	override fun initView() {
		val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
		binding.vexlGoogles.startAnimation(animation)

		setExitTransitionZSharedAxis()
	}

	override fun bindObservers() {
		repeatScopeOnCreate {
			viewModel.userFlow.collect { user ->
				if (user != null && user.finishedOnboarding) {
					Timber.i("Navigating to marketplace")
					viewModel.loadMyContactsKeys()
				} else {
					// continue to onboarding
					viewModel.deletePreviousUserKeys()

					Timber.i("Navigating to intro")
					delay(SPLASH_DELAY)
					viewModel.navMainGraphModel.navigateToGraph(
						NavMainGraphModel.NavGraph.Intro
					)
				}
			}
		}

		repeatScopeOnCreate {
			viewModel.contactKeysLoaded.collect {
				//now we need to check, if user uses old v1 offer model and has some own offers
				if (viewModel.encryptedPreferenceRepository.hasOfferV2 || viewModel.myOffers.isEmpty()) {
					continueToMarketplace()
				} else {
					//migrate v1 offers to v2 offers
					viewModel.migrateOfferToV2(viewModel.myOffers.first(), 0)
				}
			}
		}

		repeatScopeOnCreate {
			viewModel.showEncryptingDialogFlow.collect {
				//show encrypt dialog
				showBottomDialog(EncryptingProgressBottomSheetDialog(it.second, isNewOffer = true) { resource ->
					//delete just migrated offer from local DB
					viewModel.deleteMigratedOfferFromDB(viewModel.myOffers[it.first].offerId)

					//check if we have more offers to migrate
					val nextIndex = it.first.inc()
					if (nextIndex >= viewModel.myOffers.size) {
						//finally continue to Marketplace
						continueToMarketplace()
					} else {
						//migrate my next offer
						val nextMyOffer = viewModel.myOffers[nextIndex]
						viewModel.migrateOfferToV2(nextMyOffer, nextIndex)
					}
				})
			}
		}

		repeatScopeOnResume {
			viewModel.errorFlow.collect { resource ->
				if (resource.status is Status.Error) {
					//show error toast
					resource.errorIdentification.stringMessage?.let { message ->
						cz.cleevio.vexl.lightbase.core.extensions.showSnackbar(
							view = binding.container,
							message = message
						)
					}
				}
			}
		}

		repeatScopeOnCreate {
			//we don't have all data for offer migration to v2 and BE has already deleted all data, so no luck, offer is dead
			viewModel.skipMigrationOnError.collect { brokenOfferIndex ->
				val nextIndex = brokenOfferIndex.inc()
				if (nextIndex >= viewModel.myOffers.size) {
					continueToMarketplace()
				} else {
					//try to migrate my next offer
					val nextMyOffer = viewModel.myOffers[nextIndex]
					viewModel.migrateOfferToV2(nextMyOffer, nextIndex)
				}
			}
		}
	}

	override fun onResume() {
		super.onResume()

		if (viewModel.encryptedPreferenceRepository.isOfferEncrypted) {
			continueToMarketplace()
		}
	}

	private fun continueToMarketplace() {
		//reset encryption dialog flag
		viewModel.encryptedPreferenceRepository.isOfferEncrypted = false
		//mark migration from V1 as done
		viewModel.encryptedPreferenceRepository.hasOfferV2 = true

		//reencrypt only when done migrating
		viewModel.backgroundQueue.reEncryptOffers()

		lifecycleScope.launch {
			viewModel.navMainGraphModel.navigateToGraph(
				NavMainGraphModel.NavGraph.Main
			)
		}
	}
}