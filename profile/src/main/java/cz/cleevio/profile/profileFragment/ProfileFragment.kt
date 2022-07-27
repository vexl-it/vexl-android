package cz.cleevio.profile.profileFragment

import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.base.BaseGraphFragment
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.sendEmailToSupport
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.CurrencyPriceChartWidget
import cz.cleevio.core.widget.DeleteAccountBottomSheetDialog
import cz.cleevio.profile.R
import cz.cleevio.profile.databinding.FragmentProfileBinding
import cz.cleevio.profile.donateFragment.DonateBottomSheetDialog
import cz.cleevio.profile.joinFragment.JoinBottomSheetDialog
import cz.cleevio.profile.reportFragment.ReportBottomSheetDialog
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.profile.requestDataFragment.RequestDataBottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : BaseGraphFragment(R.layout.fragment_profile) {

	private val profileViewModel by viewModel<ProfileViewModel>()
	private val binding by viewBinding(FragmentProfileBinding::bind)

	override var priceChartWidget: CurrencyPriceChartWidget? = null

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
			profileViewModel.isRequesting.collect {
				if (it) {
					requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
				} else {
					requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
				}
			}
		}
	}

	override fun initView() {
		priceChartWidget = binding.priceChart

		super.initView()

		setUpAllowScreenshotsSwitch()

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
			Toast.makeText(requireContext(), "Contact import not implemented", Toast.LENGTH_SHORT)
				.show()
		}

		binding.profileFacebook.setOnClickListener {
			Toast.makeText(requireContext(), "Facebook import not implemented", Toast.LENGTH_SHORT)
				.show()
		}

		binding.profileSetPin.setOnClickListener {
			Toast.makeText(requireContext(), "Pin not implemented", Toast.LENGTH_SHORT)
				.show()
		}

		binding.profileSetCurrency.setOnClickListener {
			Toast.makeText(requireContext(), "Currency changer not implemented", Toast.LENGTH_SHORT)
				.show()
		}

		binding.profileAllowScreenshots.setOnClickListener {
			binding.profileAllowScreenshots.switch.setOnCheckedChangeListener(null)
			binding.profileAllowScreenshots.switch.isChecked = !binding.profileAllowScreenshots.switch.isChecked
			profileViewModel.updateAllowScreenshotsSettings()

			setUpAllowScreenshotsSwitch()
		}

		binding.profileTermsAndConditions.setOnClickListener {
			Toast.makeText(requireContext(), "TAC not implemented", Toast.LENGTH_SHORT)
				.show()
		}

		binding.profileFaq.setOnClickListener {
			Toast.makeText(requireContext(), "FAQ not implemented", Toast.LENGTH_SHORT)
				.show()
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

	private fun showBottomDialog(dialog: BottomSheetDialogFragment) {
		dialog.show(childFragmentManager, dialog.javaClass.simpleName)
	}

	private fun setUpAllowScreenshotsSwitch() {
		binding.profileAllowScreenshots.switch.setOnCheckedChangeListener { _, _ ->
			profileViewModel.updateAllowScreenshotsSettings()
		}
	}
}