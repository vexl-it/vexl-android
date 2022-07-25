package cz.cleevio.profile.cameraFragment

import android.annotation.SuppressLint
import android.util.SparseArray
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.JoinGroupBottomSheetDialog
import cz.cleevio.profile.R
import cz.cleevio.profile.databinding.FragmentCameraBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import lightbase.core.extensions.showToast
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.io.IOException

const val CODE_LENGTH = 6
const val DEBUG_LOGO = "https://design.chaincamp.cz/assets/img/logos/chaincamp-symbol-purple-rgb.svg?h=8b40a6ef383113c8e50e13f52566cade"

class CameraFragment : BaseFragment(R.layout.fragment_camera), KoinComponent {

	private lateinit var cameraSource: CameraSource
	private var surfaceCallback: SurfaceHolder.Callback? = null
	private var found: Boolean = false //block double trigger

	override val hasMenu: Boolean = true

	private val binding by viewBinding(FragmentCameraBinding::bind)

	override fun bindObservers() = Unit

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top)
		}

		scanQrCode()

		binding.close.setOnClickListener {
			findNavController().popBackStack()
		}

		binding.cameraEnterCode.setOnClickListener {
			//open input code screen
			findNavController().navigate(
				CameraFragmentDirections.actionCameraFragmentToJoinGroupCodeFragment()
			)
		}
	}

	@SuppressLint("MissingPermission")
	private fun scanQrCode() {
		//open camera to scan code
		val barcodeDetector = BarcodeDetector.Builder(requireContext().applicationContext)
			.setBarcodeFormats(Barcode.QR_CODE)
			.build()

		cameraSource = CameraSource.Builder(requireContext().applicationContext, barcodeDetector)
			.setFacing(CameraSource.CAMERA_FACING_BACK)
			.setRequestedFps(BARCODE_FPS)
			.setAutoFocusEnabled(true)
			.build()

		surfaceCallback = object : SurfaceHolder.Callback {
			@SuppressLint("MissingPermission")
			override fun surfaceCreated(holder: SurfaceHolder) {
				try {
					cameraSource.start(binding.cameraView.holder)
				} catch (e: IOException) {
					Timber.e(e)
				}
			}

			override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

			override fun surfaceDestroyed(holder: SurfaceHolder) {
				cameraSource.stop()
			}
		}

		if (binding.cameraView.holder.surface.isValid) {
			cameraSource.start(binding.cameraView.holder)
		} else {
			binding.cameraView.holder.addCallback(surfaceCallback)
		}

		barcodeDetector.setProcessor(object : Detector.Processor<Barcode?> {
			override fun release() = Unit
			override fun receiveDetections(detections: Detector.Detections<Barcode?>) {
				val barcodes = detections.detectedItems
				searchForQRCode(barcodes)
			}
		})
	}

	private fun searchForQRCode(barcodes: SparseArray<Barcode?>) {
		if (!found && barcodes.isNotEmpty()) {
			barcodes.forEach { _, value ->
				value?.rawValue?.let { data ->
					processRawValue(data)
				}
			}
		}
	}

	private fun processRawValue(rawValue: String) {
		try {
			//check that raw value is 6 numbers
			if (rawValue.isNotBlank() && rawValue.filter { it.isDigit() }.length == CODE_LENGTH) {
				found = true
				requireActivity().runOnUiThread {
					cameraSource.stop()
					Timber.tag("ASDX").d("$rawValue")
					//todo: somehow load group name and logo from code
					showBottomDialog(
						JoinGroupBottomSheetDialog(
							groupName = "TODO: $rawValue",
							groupLogo = DEBUG_LOGO,
							groupCode = rawValue.toLong(),
						)
					)
				}
			} else {
				requireActivity().runOnUiThread {
					showToast(getString(R.string.groups_scan_not_found), Toast.LENGTH_LONG)
				}
			}
		} catch (e: Exception) {
			Timber.e(e)
		}
	}

	private fun showBottomDialog(dialog: BottomSheetDialogFragment) {
		dialog.show(childFragmentManager, dialog.javaClass.simpleName)
	}

	companion object {
		private const val BARCODE_FPS = 15.0f
	}
}
