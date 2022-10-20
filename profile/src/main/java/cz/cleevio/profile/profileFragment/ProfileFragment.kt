package cz.cleevio.profile.profileFragment

import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import coil.ImageLoader
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.cleevio.core.RemoteConfigConstants
import cz.cleevio.core.base.BaseGraphFragment
import cz.cleevio.core.model.OpenedFromScreen
import cz.cleevio.core.utils.*
import cz.cleevio.core.widget.CurrencyPriceChartWidget
import cz.cleevio.core.widget.DeleteAccountBottomSheetDialog
import cz.cleevio.profile.R
import cz.cleevio.profile.currencyFragment.CurrencyBottomSheetDialog
import cz.cleevio.profile.databinding.FragmentProfileBinding
import cz.cleevio.profile.joinFragment.JoinBottomSheetDialog
import cz.cleevio.profile.profileContactsListFragment.ProfileContactsListFragment
import cz.cleevio.profile.profileFacebookContactsListFragment.ProfileFacebookContactsListFragment
import cz.cleevio.profile.reportFragment.ReportBottomSheetDialog
import cz.cleevio.profile.requestDataFragment.RequestDataBottomSheetDialog
import cz.cleevio.repository.RandomUtils
import cz.cleevio.repository.model.Currency.Companion.mapStringToCurrency
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.utils.PermissionResolver
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : BaseGraphFragment(R.layout.fragment_profile) {

	private val profileViewModel by viewModel<ProfileViewModel>()
	private val binding by viewBinding(FragmentProfileBinding::bind)

	override var priceChartWidget: CurrencyPriceChartWidget? = null

	private val requestContactsPermissions = registerForActivityResult(
		ActivityResultContracts.RequestMultiplePermissions()
	) { permissions ->
		PermissionResolver.resolve(
			requireActivity(),
			permissions,
			allGranted = {
				profileViewModel.updateHasReadContactPermissions(true)
			},
			denied = {
				profileViewModel.updateHasReadContactPermissions(false)
			},
			permanentlyDenied = {
				profileViewModel.updateHasReadContactPermissions(false)
			}
		)
	}

	override fun bindObservers() {
		super.bindObservers()

		repeatScopeOnStart {
			profileViewModel.userFlow.collect {
				it?.let { user ->
					// TODO show anonymous user name if there will be difference for public/private profile
					binding.profileUserName.text = user.username

					when (user.avatarBase64 == null) {
						true -> {
							val anonymousImageIndex = user.anonymousAvatarImageIndex
							if (anonymousImageIndex != null) {
								binding.profileUserPhoto.load(
									drawableResId = RandomUtils.getRandomImageDrawableId(anonymousImageIndex),
									imageLoader = ImageLoader.invoke(requireContext())
								) {
									setPlaceholders(R.drawable.random_avatar_3)
								}
							} else {
								binding.profileUserPhoto.load(
									drawableResId = R.drawable.random_avatar_3,
									imageLoader = ImageLoader.invoke(requireContext())
								) {
									setPlaceholders(R.drawable.random_avatar_3)
								}
							}
						}
						false -> {
							binding.profileUserPhoto.load(user.avatarBase64?.getBitmap()) {
								setPlaceholders(R.drawable.random_avatar_3)
							}
						}
					}
				}
			}
		}
		repeatScopeOnStart {
			profileViewModel.contactsNumber.collect {
				binding.profileContacts.setSubtitle(
					getString(R.string.profile_import_contacts_subtitle, it.toString())
				)
			}
		}
		repeatScopeOnStart {
			profileViewModel.hasPermissionsEvent.collect { hasPermission ->
				if (hasPermission) {
					showBottomDialog(
						ProfileContactsListFragment(OpenedFromScreen.PROFILE)
					)
				} else {
					showPermissionDeniedDialog()
				}
			}
		}
		repeatScopeOnStart {
			profileViewModel.encryptedPreferenceRepository.numberOfImportedContactsFlow.collect { contacts ->
				binding.profileContacts.setSubtitle(getString(R.string.profile_import_contacts_subtitle, contacts.toString()))
			}
		}
	}

	override fun initView() {
		priceChartWidget = binding.priceChart

		super.initView()

		// TODO change visibility once the functionality will be revealed
		binding.profileSectionWrapperPrivateSettings.isVisible = false
		binding.profileSectionWrapperFive.isVisible = false
		binding.profileFacebook.isVisible = false

		binding.profileQrCode.setOnClickListener {
			showBottomDialog(
				JoinBottomSheetDialog()
			)
		}

		context?.packageManager?.getPackageInfo(context?.packageName ?: "", 0)?.versionName?.let {
			binding.profileAppVersion.isVisible = true
			binding.profileAppVersion.text = getString(R.string.profile_app_version, it)
		}

		priceChartWidget?.onLayoutChanged = {
			TransitionManager.beginDelayedTransition(binding.container)
		}

		binding.profilePrivateSettings.setOnClickListener {
			// TODO implement private settings
			Toast.makeText(requireContext(), "Profile private settings not implemented", Toast.LENGTH_SHORT)
				.show()
		}
		binding.profileGroups.setOnClickListener {
			if (profileViewModel.remoteConfig.getBoolean(RemoteConfigConstants.MARKETPLACE_LOCKED)) {
				Toast.makeText(requireContext(), getString(R.string.locked_groups), Toast.LENGTH_SHORT)
					.show()
			} else {
				findNavController().safeNavigateWithTransition(
					ProfileFragmentDirections.actionProfileFragmentToGroupFragment()
				)
			}
		}
		binding.profileChangePicture.setOnClickListener {
			findNavController().safeNavigateWithTransition(
				ProfileFragmentDirections.actionProfileFragmentToEditAvatarFragment()
			)
		}
		binding.profileEditName.setOnClickListener {
			findNavController().safeNavigateWithTransition(
				ProfileFragmentDirections.actionProfileFragmentToEditNameFragment()
			)
		}
		binding.profileContacts.setOnClickListener {
			checkReadContactsPermissions()
		}
		binding.profileFacebook.setOnClickListener {
			showBottomDialog(
				ProfileFacebookContactsListFragment()
			)
		}
		binding.profileSetPin.setOnClickListener {
			// TODO implement PIN
		}
		binding.profileSetCurrency.setOnClickListener {
			showBottomDialog(
				CurrencyBottomSheetDialog(viewModel.encryptedPreferenceRepository.selectedCurrency.mapStringToCurrency()) {
					profileViewModel.setCurrency(it)
					viewModel.syncMarketData()
				}
			)
		}
		binding.profileAllowScreenshots.setOnClickListener {
			binding.profileAllowScreenshots.switch.setOnCheckedChangeListener(null)
			profileViewModel.updateAllowScreenshotsSettings()
			binding.profileAllowScreenshots.switch.isChecked = profileViewModel.areScreenshotsAllowed

			setUpAllowScreenshotsSwitch()
		}
		binding.profileTermsAndConditions.setOnClickListener {
			findNavController().safeNavigateWithTransition(
				ProfileFragmentDirections.actionProfileFragmentToTermsFragment()
			)
		}
		binding.profileFaq.setOnClickListener {
			findNavController().safeNavigateWithTransition(
				ProfileFragmentDirections.actionProfileFragmentToFaqFragment(
					continueToOnboarding = false
				)
			)
		}
		binding.profileReportIssue.setOnClickListener {
			showBottomDialog(
				ReportBottomSheetDialog()
			)
		}
		binding.profileInAppLogging.setOnClickListener {
			findNavController().safeNavigateWithTransition(
				ProfileFragmentDirections.actionProfileFragmentToLogFragment()
			)
		}
		binding.profileRequestData.setOnClickListener {
			showBottomDialog(
				RequestDataBottomSheetDialog()
			)
		}
		binding.profileTwitter.setDebouncedOnClickListener {
			openWeb(TWITTER)
		}
		binding.profileMedium.setDebouncedOnClickListener {
			openWeb(MEDIUM)
		}
		binding.profileVexlIt.setDebouncedOnClickListener {
			openWeb(VEXL_IT)
		}
		binding.profileLogout.setDebouncedOnClickListener {
			showBottomDialog(
				DeleteAccountBottomSheetDialog {
					if (it) {
						profileViewModel.logout()
					}
				}
			)
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top)
		}
	}

	override fun onResume() {
		super.onResume()

		binding.profileAllowScreenshots.switch.setOnCheckedChangeListener(null)
		binding.profileAllowScreenshots.switch.isChecked = profileViewModel.areScreenshotsAllowed
		setUpAllowScreenshotsSwitch()

		binding.profileContacts.setOnClickListener {
			checkReadContactsPermissions()
		}
	}

	private fun setUpAllowScreenshotsSwitch() {
		binding.profileAllowScreenshots.switch.setOnCheckedChangeListener { _, _ ->
			binding.profileAllowScreenshots.callOnClick()
		}
	}

	private fun checkReadContactsPermissions() {
		requestContactsPermissions.launch(arrayOf(Manifest.permission.READ_CONTACTS))
	}

	private fun showPermissionDeniedDialog() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.import_contacts_request_title)
			.setMessage(R.string.import_contacts_request_description)
			.setNegativeButton(R.string.import_contacts_not_allow) { dialog, _ ->
				dialog.dismiss()
			}
			.setPositiveButton(R.string.import_contacts_allow) { _, _ ->
				checkReadContactsPermissions()
			}
			.show()
	}

	companion object {
		private const val TWITTER = "https://twitter.com/vexl"
		private const val MEDIUM = "https://blog.vexl.it"
		private const val VEXL_IT = "https://vexl.it"
	}
}
