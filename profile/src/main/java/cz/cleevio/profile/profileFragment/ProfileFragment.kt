package cz.cleevio.profile.profileFragment

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.cleevio.core.base.BaseGraphFragment
import cz.cleevio.core.model.Currency.Companion.mapStringToCurrency
import cz.cleevio.core.model.OpenedFromScreen
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.CurrencyPriceChartWidget
import cz.cleevio.core.widget.DeleteAccountBottomSheetDialog
import cz.cleevio.profile.R
import cz.cleevio.profile.currencyFragment.CurrencyBottomSheetDialog
import cz.cleevio.profile.databinding.FragmentProfileBinding
import cz.cleevio.profile.donateFragment.DonateBottomSheetDialog
import cz.cleevio.profile.joinFragment.JoinBottomSheetDialog
import cz.cleevio.profile.profileContactsListFragment.ProfileContactsListFragment
import cz.cleevio.profile.profileFacebookContactsListFragment.ProfileFacebookContactsListFragment
import cz.cleevio.profile.reportFragment.ReportBottomSheetDialog
import cz.cleevio.profile.requestDataFragment.RequestDataBottomSheetDialog
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.utils.PermissionResolver
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ProfileFragment : BaseGraphFragment(R.layout.fragment_profile) {

	private val profileViewModel by viewModel<ProfileViewModel>()
	private val binding by viewBinding(FragmentProfileBinding::bind)

	override var priceChartWidget: CurrencyPriceChartWidget? = null

	private val requestContactsPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
		PermissionResolver.resolve(requireActivity(), permissions,
			allGranted = {
				profileViewModel.updateHasReadContactPermissions(true)
			},
			denied = {
				profileViewModel.updateHasReadContactPermissions(false)
			},
			permanentlyDenied = {
				profileViewModel.updateHasReadContactPermissions(false)
			})
	}

	override fun bindObservers() {
		super.bindObservers()
		repeatScopeOnStart {
			profileViewModel.userFlow.collect {
				it?.let { user ->
					binding.profileUserName.text = user.username
					//todo: this should convert from base64 to bitmap
					binding.profileUserPhoto.load(user.avatar) {
						crossfade(true)
						fallback(R.drawable.ic_baseline_person_128)
						error(R.drawable.ic_baseline_person_128)
						placeholder(R.drawable.ic_baseline_person_128)
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
			profileViewModel.hasPermissionsEvent.collect { hasPermisson ->
				if (hasPermisson) {
					Timber.tag("ASDX").d("Permission granted")
					showBottomDialog(
						ProfileContactsListFragment(OpenedFromScreen.PROFILE)
					)
				} else {
					Timber.tag("ASDX").d("Permission rejected")
					showPermissionDeniedDialog()
				}
			}
		}
	}

	override fun initView() {
		priceChartWidget = binding.priceChart

		super.initView()

		binding.profileAllowScreenshots.switch.setOnCheckedChangeListener(null)
		binding.profileAllowScreenshots.switch.isChecked = profileViewModel.areScreenshotsAllowed
		setUpAllowScreenshotsSwitch()

		// TODO change visibility once the functionality will be revealed
		binding.profileSectionWrapperFive.isVisible = false

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}

		binding.profileDonate.setOnClickListener {
			showBottomDialog(
				DonateBottomSheetDialog {
					if (it) {
						startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://seznam.cz")))
					}
				}
			)
		}

		binding.profileQrCode.setOnClickListener {
			showBottomDialog(
				JoinBottomSheetDialog()
			)
		}

		binding.profilePrivateSettings.setOnClickListener {
			Toast.makeText(requireContext(), "Profile private settings not implemented", Toast.LENGTH_SHORT)
				.show()
		}

		binding.profileGroups.setOnClickListener {
			findNavController().navigate(
				ProfileFragmentDirections.actionProfileFragmentToGroupFragment()
			)
		}

		binding.profileChangePicture.setOnClickListener {
			findNavController().navigate(
				ProfileFragmentDirections.actionProfileFragmentToEditAvatarFragment()
			)
		}

		binding.profileEditName.setOnClickListener {
			findNavController().navigate(
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
			Toast.makeText(requireContext(), "Pin not implemented", Toast.LENGTH_SHORT)
				.show()
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
				ProfileFragmentDirections.actionProfileFragmentToFaqFragment()
			)
		}

		binding.profileReportIssue.setOnClickListener {
			showBottomDialog(
				ReportBottomSheetDialog()
			)
		}

		binding.profileRequestData.setOnClickListener {
			showBottomDialog(
				RequestDataBottomSheetDialog()
			)
		}

		binding.profileLogout.setOnClickListener {
			showBottomDialog(
				DeleteAccountBottomSheetDialog {
					if (it) {
						profileViewModel.logout(
							{
								profileViewModel.navigateToOnboarding()
							},
							{
								profileViewModel.navigateToOnboarding()
							}
						)
					}
				}
			)
		}
	}

	override fun onResume() {
		super.onResume()

		binding.profileContacts.setOnClickListener {
			checkReadContactsPermissions()
		}
	}

	private fun showBottomDialog(dialog: BottomSheetDialogFragment) {
		dialog.show(childFragmentManager, dialog.javaClass.simpleName)
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
}
