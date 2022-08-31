package cz.cleevio.profile.cameraFragment

import android.Manifest
import android.annotation.SuppressLint
import android.util.SparseArray
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.JoinGroupBottomSheetDialog
import cz.cleevio.profile.R
import cz.cleevio.profile.databinding.FragmentCameraBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.extensions.openAppSettings
import cz.cleevio.vexl.lightbase.core.extensions.showSnackbar
import cz.cleevio.vexl.lightbase.core.extensions.showToast
import cz.cleevio.vexl.lightbase.core.utils.PermissionResolver
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.io.IOException

const val CODE_LENGTH = 6
const val PERMANENTLY_DENIED_DURATION = 5000

class CameraFragment : BaseFragment(R.layout.fragment_camera), KoinComponent {

	private lateinit var cameraSource: CameraSource
	private var surfaceCallback: SurfaceHolder.Callback? = null
	private var found: Boolean = false //block double trigger

	private val binding by viewBinding(FragmentCameraBinding::bind)
	override val viewModel by viewModel<CameraViewModel>()

	private val requestCameraPermissions =
		registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
			PermissionResolver.resolve(
				requireActivity(),
				permissions,
				allGranted = {
					binding.cameraDenied.isVisible = false
					scanQrCode()
				},
				denied = {
					binding.cameraDenied.isVisible = true
				},
				permanentlyDenied = {
					binding.cameraDenied.isVisible = true
					showSnackbar(
						binding.container,
						getString(lightbase.camera.R.string.camera_error_permanent_ban_camera),
						anchorView = binding.container,
						buttonText = lightbase.camera.R.string.camera_open_settings,
						action = {
							requireActivity().openAppSettings()
						},
						duration = PERMANENTLY_DENIED_DURATION,
						onDismissCallback = {

						}
					)
				})
		}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.groupLoaded.collect { group ->
				showBottomDialog(
					JoinGroupBottomSheetDialog(
						groupName = group.name,
						groupLogo = group.logoUrl ?: "",
						groupCode = group.code,
						destinationId = R.id.groupFragment
					)
				)
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top)
		}

		binding.close.setOnClickListener {
			findNavController().popBackStack()
		}

		binding.cameraEnterCode.setOnClickListener {
			//open input code screen
			findNavController().navigate(
				CameraFragmentDirections.actionCameraFragmentToJoinGroupCodeFragment()
			)
		}

		binding.container.post {
			requestCameraPermissions.launch(arrayOf(Manifest.permission.CAMERA))
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
			Timber.tag("ASDX").d("$rawValue")
			val code = rawValue.takeLast(CODE_LENGTH)
			//check that code value is 6 numbers
			if (code.isNotBlank() && code.filter { it.isDigit() }.length == CODE_LENGTH) {
				found = true
				requireActivity().runOnUiThread {
					cameraSource.stop()
					viewModel.loadGroupByCode(code)
				}
			} else {
				requireActivity().runOnUiThread {
					showToast(getString(R.string.groups_scan_not_found), Toast.LENGTH_LONG)
				}
			}
		} catch (e: Exception) {
			Timber.e(e)
			requireActivity().runOnUiThread {
				showToast(getString(R.string.groups_scan_not_found), Toast.LENGTH_LONG)
			}
		}
	}

	companion object {
		private const val BARCODE_FPS = 15.0f
	}
}
