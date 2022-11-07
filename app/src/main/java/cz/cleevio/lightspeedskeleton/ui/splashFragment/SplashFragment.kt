package cz.cleevio.lightspeedskeleton.ui.splashFragment

import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import cz.cleevio.core.utils.*
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.FragmentSplashBinding
import cz.cleevio.lightspeedskeleton.ui.checkForMaintenance
import cz.cleevio.lightspeedskeleton.ui.checkForUpdate
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
				when {
					checkForUpdate(viewModel.remoteConfig) -> {
						viewModel.navMainGraphModel.navigateToGraph(
							NavMainGraphModel.NavGraph.ForceUpdate
						)
					}
					checkForMaintenance(viewModel.remoteConfig) -> {
						viewModel.navMainGraphModel.navigateToGraph(
							NavMainGraphModel.NavGraph.Maintenance
						)
					}
					else -> {
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
			}
		}

		repeatScopeOnCreate {
			viewModel.contactKeysLoaded.collect {
				Timber.tag("REENCRYPT_MIGRATION").d("keys loaded, hasOfferV2: ${viewModel.encryptedPreferenceRepository.hasOfferV2}, offers1 empty: ${viewModel.myOffersV1.isEmpty()}")
				//now we need to check, if user uses old v1 offer model and has some own offers
				if (viewModel.encryptedPreferenceRepository.hasOfferV2 || viewModel.myOffersV1.isEmpty()) {
					continueToMarketplace()
				} else {
					//migrate v1 offers to v2 offers
					viewModel.migrateOfferToV2(viewModel.myOffersV1.first(), 0)
				}
			}
		}

		repeatScopeOnCreate {
			viewModel.showEncryptingDialogFlow.collect {
				Timber.tag("REENCRYPT_MIGRATION").d("showBottomDialog for offer index: ${it.first}")
				//show encrypt dialog
				showBottomDialog(EncryptingProgressBottomSheetDialog(it.second, isNewOffer = true) { resource ->

					if (resource.status == Status.Success) {
						Timber.tag("REENCRYPT_MIGRATION").d("migration was success for offer index: ${it.first}")
					}
					if (resource.status == Status.Error) {
						Timber.tag("REENCRYPT_MIGRATION").d("migration was error for offer index: ${it.first}")
					}
					//delete just migrated offer from local DB
					Timber.tag("REENCRYPT_MIGRATION").d("deleting offer index ${it.first} because migration ended: ${viewModel.myOffersV1[it.first].offerId}}")
					viewModel.deleteMigratedOfferFromDB(viewModel.myOffersV1[it.first].offerId)

					//check if we have more offers to migrate
					val nextIndex = it.first.inc()
					Timber.tag("REENCRYPT_MIGRATION").d("nextIndex is ${nextIndex}, size is ${viewModel.myOffersV1.size}")
					if (nextIndex >= viewModel.myOffersV1.size) {
						//finally continue to Marketplace
						Timber.tag("REENCRYPT_MIGRATION").d("all done, go to marketplace")
						continueToMarketplace()
					} else {
						//migrate my next offer
						Timber.tag("REENCRYPT_MIGRATION").d("more offers, continue with migration")
						val nextMyOffer = viewModel.myOffersV1[nextIndex]
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
				Timber.tag("REENCRYPT_MIGRATION").d("skipMigrationOnError: ${brokenOfferIndex}")
				val nextIndex = brokenOfferIndex.inc()
				Timber.tag("REENCRYPT_MIGRATION").d("skipMigrationOnError nextIndex is ${nextIndex}, size is ${viewModel.myOffersV1.size}")
				if (nextIndex >= viewModel.myOffersV1.size) {
					continueToMarketplace()
				} else {
					//try to migrate my next offer
					val nextMyOffer = viewModel.myOffersV1[nextIndex]
					viewModel.migrateOfferToV2(nextMyOffer, nextIndex)
				}
			}
		}
	}

	override fun onResume() {
		super.onResume()

		Timber.tag("REENCRYPT_MIGRATION").d("onResume isOfferEncrypted: ${viewModel.encryptedPreferenceRepository.isOfferEncrypted}")
		if (viewModel.encryptedPreferenceRepository.isOfferEncrypted) {
			continueToMarketplace()
		}
	}

	private fun continueToMarketplace() {
		//reset encryption dialog flag
		viewModel.encryptedPreferenceRepository.isOfferEncrypted = false
		//mark migration from V1 as done
		viewModel.encryptedPreferenceRepository.hasOfferV2 = true
		Timber.tag("REENCRYPT_MIGRATION").d("continue to marketplace, hasOfferV2: ${viewModel.encryptedPreferenceRepository.hasOfferV2}")

		//reencrypt only when done migrating
		viewModel.backgroundQueue.reEncryptOffers()

		lifecycleScope.launch {
			viewModel.navMainGraphModel.navigateToGraph(
				NavMainGraphModel.NavGraph.Main
			)
		}
	}
}