package lightbase.camera.ui.takePhotoFragment

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.openAppSettings
import cz.cleevio.vexl.lightbase.core.extensions.showSnackbar
import cz.cleevio.vexl.lightbase.core.utils.PermissionResolver
import lightbase.camera.R
import lightbase.camera.databinding.FragmentTakePhotoBinding
import lightbase.camera.utils.CameraUtils
import lightbase.camera.utils.viewBinding
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TakePhotoFragment : BaseFragment(R.layout.fragment_take_photo) {

	private val binding by viewBinding(FragmentTakePhotoBinding::bind)
	private val args by navArgs<TakePhotoFragmentArgs>()

	private var imageCapture: ImageCapture? = null
	private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
	private lateinit var cameraExecutor: ExecutorService
	private var windowRunnable: Runnable? = null
	private var foregroundRunnable1: Runnable? = null
	private var foregroundRunnable2: Runnable? = null
	private val requestCameraPermissions =
		registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
			PermissionResolver.resolve(
				requireActivity(),
				permissions,
				allGranted = {
					startCamera()
				},
				denied = {
					setFragmentResult(RESULT_CAMERA_RESULT, TakePhotoResult.toBundle(TakePhotoResult.Denied))
					findNavController().popBackStack()
				},
				permanentlyDenied = {
					showSnackbar(
						binding.selfieContainer,
						getString(R.string.camera_error_permanent_ban_camera),
						anchorView = binding.selfieContainer,
						buttonText = R.string.camera_open_settings,
						action = {
							requireActivity().openAppSettings()
						},
						duration = 5000,
						onDismissCallback = {
							setFragmentResult(RESULT_CAMERA_RESULT, TakePhotoResult.toBundle(TakePhotoResult.PermanentlyDenied))
							findNavController().popBackStack()
						}
					)
				})
		}

	override fun initView() {
		setViewForBottomInset(binding.selfieCameraButtonContainer)
		setViewForTopInset(binding.selfieContainer)

		lensFacing = args.cameraOrientation

		cameraExecutor = Executors.newSingleThreadExecutor()

		binding.selfiePreviewView.post {
			requestCameraPermissions.launch(arrayOf(Manifest.permission.CAMERA))

			// Set up the listener for take photo button
			binding.selfieCameraButton.setOnClickListener { takePhoto(requireContext()) }
		}

		setupCloseButton()
	}

	private fun setupCloseButton() {
		binding.closeBtn.isVisible = args.closeBtnVisible
		if (args.closeIconResId != 0) {
			binding.closeBtn.setImageResource(args.closeIconResId)
		}
		binding.closeBtn.setOnClickListener {
			setFragmentResult(RESULT_CAMERA_RESULT, TakePhotoResult.toBundle(TakePhotoResult.ManuallyClosed))
			findNavController().popBackStack()
		}
	}

	override fun bindObservers() = Unit

	@Suppress("DEPRECATION")
	override fun onResume() {
		super.onResume()

		// Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
		// be trying to set app to immersive mode before it's ready and the flags do not stick
		windowRunnable = Runnable {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				requireActivity().window.setDecorFitsSystemWindows(false)
			} else {
				binding.selfieContainer.systemUiVisibility =
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
						View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
						View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			}
		}
		binding.selfieContainer.postDelayed(windowRunnable, 500L)
	}

	override fun onStop() {
		super.onStop()
		windowRunnable?.let { binding.selfieContainer.removeCallbacks(it) }
		foregroundRunnable1?.let { binding.selfieForeground.removeCallbacks(it) }
		foregroundRunnable2?.let { binding.selfieForeground.removeCallbacks(it) }
	}

	override fun onDestroy() {
		super.onDestroy()
		cameraExecutor.shutdown()
	}

	@Suppress("DEPRECATION")
	private fun getOutputDirectory(context: Context): File {
		// Use external media if it is available, our app's file directory otherwise
		val appContext = context.applicationContext
		val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
			File(
				it,
				args.projectName
			).apply { mkdirs() }
		}
		return if (mediaDir != null && mediaDir.exists())
			mediaDir else appContext.filesDir
	}

	private fun takePhoto(context: Context) {
		// Get a stable reference of the modifiable image capture use case
		val imageCapture = imageCapture ?: return

		// Setup image capture metadata
		val metadata = ImageCapture.Metadata().apply {
			// Mirror image when using the front camera
			isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
		}

		val photoFile = File(getOutputDirectory(requireContext()), getFileName())

		// Create the output file option to store the captured image in MediaStore
		val outputFileOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			// Create time-stamped output file to hold the image
			val contentValues = ContentValues().apply {
				put(MediaStore.MediaColumns.DISPLAY_NAME, getFileName())
				put(MediaStore.MediaColumns.MIME_TYPE, PHOTO_MIME_TYPE)
			}
			ImageCapture.OutputFileOptions.Builder(
				context.contentResolver,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				contentValues
			).setMetadata(metadata)
				.build()
		} else {
			ImageCapture.OutputFileOptions.Builder(photoFile)
				.setMetadata(metadata)
				.build()
		}

		// Set up image capture listener, which is triggered after photo has been taken
		imageCapture.takePicture(
			outputFileOptions,
			ContextCompat.getMainExecutor(context),
			object : ImageCapture.OnImageSavedCallback {

				override fun onImageSaved(output: ImageCapture.OutputFileResults) {
					val uri = output.savedUri ?: Uri.fromFile(photoFile)
					setFragmentResult(RESULT_CAMERA_RESULT, TakePhotoResult.toBundle(TakePhotoResult.Success(uri.toString())))
					requireActivity().runOnUiThread {
						findNavController().popBackStack()
					}
				}

				override fun onError(e: ImageCaptureException) {
					Timber.e(e)
				}
			}
		)

		foregroundRunnable2 = Runnable {
			binding.selfieForeground.isVisible = false
		}
		foregroundRunnable1 = Runnable {
			binding.selfieForeground.isVisible = true
			binding.selfieContainer.postDelayed(foregroundRunnable2, 50L)
		}
		binding.selfieForeground.postDelayed(foregroundRunnable1, 100L)
	}

	private fun startCamera() {
		val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
		imageCapture = ImageCapture.Builder()
			.build()

		cameraProviderFuture.addListener({
			// Used to bind the lifecycle of cameras to the lifecycle owner
			val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
			// Select back camera as a default
			val cameraSelector = CameraSelector.Builder()
				.requireLensFacing(lensFacing)
				.build()

			// Get screen metrics used to setup camera for full screen resolution
			val metrics = DisplayMetrics().also { binding.selfiePreviewView.display.getRealMetrics(it) }
			val screenAspectRatio = CameraUtils.aspectRatio(metrics.widthPixels, metrics.heightPixels)
			val rotation = binding.selfiePreviewView.display.rotation

			// Preview
			val preview = Preview.Builder()
				// We request aspect ratio but no resolution
				.setTargetAspectRatio(screenAspectRatio)
				// Set initial target rotation
				.setTargetRotation(rotation)
				.build()
				.also {
					it.setSurfaceProvider(binding.selfiePreviewView.surfaceProvider)
				}

			// ImageCapture
			imageCapture = ImageCapture.Builder()
				.setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
				// We request aspect ratio but no resolution to match preview config, but letting
				// CameraX optimize for whatever specific resolution best fits our use cases
				.setTargetAspectRatio(screenAspectRatio)
				// Set initial target rotation, we will have to call this again if rotation changes
				// during the lifecycle of this use case
				.setTargetRotation(rotation)
				.build()

			try {
				// Unbind use cases before rebinding
				cameraProvider.unbindAll()

				// Bind use cases to camera
				cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
			} catch (e: Exception) {
				Timber.e(e)
			}
		}, ContextCompat.getMainExecutor(requireContext()))
	}

	companion object {
		private const val PHOTO_EXTENSION = ".jpg"
		private const val PHOTO_MIME_TYPE = "image/jpg"
		private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

		const val RESULT_CAMERA_RESULT = "result_camera_result"

		private fun getFileName(): String {
			return SimpleDateFormat(FILENAME_FORMAT, Locale.US)
				.format(System.currentTimeMillis()) + PHOTO_EXTENSION
		}
	}
}