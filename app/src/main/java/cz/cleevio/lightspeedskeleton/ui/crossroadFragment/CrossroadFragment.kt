package cz.cleevio.lightspeedskeleton.ui.crossroadFragment

import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.Barcode
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.FragmentCrossroadBinding
import lightbase.camera.ui.barcodeScanner.BarcodeScannerFragmentArgs
import lightbase.camera.ui.takePhotoFragment.TakePhotoFragment
import lightbase.camera.ui.takePhotoFragment.TakePhotoResult
import lightbase.core.baseClasses.BaseFragment
import lightbase.countrypicker.ui.CountryPickerBottomSheetDialogArgs
import timber.log.Timber

class CrossroadFragment : BaseFragment(R.layout.fragment_crossroad) {

	private val binding by viewBinding(FragmentCrossroadBinding::bind)

	override fun bindObservers() = Unit

	override fun initView() {
		setViewForTopInset(binding.container)

		binding.btnUiTemplates.setOnClickListener {
			findNavController().navigate(
				CrossroadFragmentDirections.actionCrossroadFragmentToUITemplatesFragment()
			)
		}

		binding.btnPinScreen.setOnClickListener {
			findNavController().navigate(
				CrossroadFragmentDirections.actionCrossroadFragmentToPinFragment()
			)
		}

		binding.btnPhotoOptions.setOnClickListener {
			findNavController().navigate(
				CrossroadFragmentDirections.actionCrossroadFragmentToNavPhotoBottomSheet()
			)
		}

		binding.btnSelfieCamera.setOnClickListener {
			findNavController().navigate(
				CrossroadFragmentDirections.actionCrossroadFragmentToNavCamera(getString(R.string.app_name))
			)
		}

		binding.btnImageHelper.setOnClickListener {
			findNavController().navigate(
				CrossroadFragmentDirections.actionCrossroadFragmentToImageHelperFragment()
			)
		}

		binding.btnListTemplate.setOnClickListener {
			findNavController().navigate(
				CrossroadFragmentDirections.actionCrossroadFragmentToListTemplateFragment()
			)
		}

		setupListeners()
	}

	private fun setupListeners() {
		setFragmentResultListener(TakePhotoFragment.RESULT_CAMERA_RESULT) { _, bundle ->
			val result = TakePhotoResult.fromBundle(bundle)
			Timber.d("take photo result $result")
			when (result) {
				is TakePhotoResult.Success -> {
				}
				is TakePhotoResult.Denied -> {
				}
				is TakePhotoResult.ManuallyClosed -> {
				}
				is TakePhotoResult.PermanentlyDenied -> {
				}
			}
		}

		binding.btnCountryPicker.setOnClickListener {
			findNavController().navigate(
				R.id.nav_country_picker,
				CountryPickerBottomSheetDialogArgs(
					titleResId = R.string.country_picker_title,
					searchHintResId = R.string.country_picker_title,
					selectedIconResId = R.drawable.ic_check_24dp,
					localeCountryFirst = true,
					showDialCode = true,
					searchEnabled = true,
					selectedCountryCode = "CZ",
					supportedUnknownCountry = true,
					//supportedCountryCodes = arrayOf("cz", "zm", "us", "CA")
				).toBundle()
			)
		}

		binding.btnCodeBarcodeAnalyzer.setOnClickListener {
			findNavController().navigate(
				R.id.nav_barcode_scanner,
				BarcodeScannerFragmentArgs(
					barcodeFormat = intArrayOf(Barcode.FORMAT_QR_CODE),
					expectedBarcodeValue = "1111"
				).toBundle()
			)
		}
	}
}