package cz.cleevio.lightspeedskeleton.ui.imageHelperFragment

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.FragmentImageHelperBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lightbase.camera.utils.ImageHelper
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.showToast
import lightbase.core.utils.PermissionResolver
import org.koin.android.ext.android.inject

class ImageHelperFragment : BaseFragment(R.layout.fragment_image_helper) {

	private val imageHelper by inject<ImageHelper>()
	private val binding by viewBinding(FragmentImageHelperBinding::bind)
	private var permissionOption: PermissionOption? = null
	private var currentPhotoPath: Uri? = null

	private val pickImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		result.data?.data?.let { uri -> currentPhotoPath = uri }

		viewLifecycleOwner.lifecycleScope.launch {
			val bitmap = imageHelper.loadImage(requireActivity(), currentPhotoPath!!)
			withContext(Dispatchers.Main) {
				binding.imageView.setImageBitmap(bitmap)
			}
		}
	}

	private val requestCameraPermissions =
		registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
			PermissionResolver.resolve(
				requireActivity(),
				permissions,
				allGranted = {
					when (permissionOption) {
						PermissionOption.CAMERA -> {
							val (intent, uri) = imageHelper.createCameraIntent(requireActivity(), false, "Some text:")
							currentPhotoPath = uri
							pickImageResult.launch(intent)
						}
						PermissionOption.GALLERY -> {
							val photoGalleryIntent = imageHelper.createGalleryIntent()
							pickImageResult.launch(photoGalleryIntent)
						}
					}
				},
				denied = {
					Toast.makeText(requireActivity(), "denied", Toast.LENGTH_SHORT).show()
				},
				permanentlyDenied = {
					Toast.makeText(requireActivity(), "permanently denied", Toast.LENGTH_SHORT).show()
				})
		}

	override fun bindObservers() = Unit

	override fun initView() {
		setViewForTopInset(binding.container)

		binding.cameraIntent.setOnClickListener {
			permissionOption = PermissionOption.CAMERA
			requestCameraPermissions.launch(
				arrayOf(
					Manifest.permission.CAMERA,
					Manifest.permission.READ_EXTERNAL_STORAGE
				)
			)
		}

		binding.galleryIntent.setOnClickListener {
			permissionOption = PermissionOption.GALLERY
			requestCameraPermissions.launch(
				arrayOf(
					Manifest.permission.READ_EXTERNAL_STORAGE
				)
			)
		}

		binding.shareImage.setOnClickListener {
			if (currentPhotoPath == null) {
				showToast("Load some image first")
				return@setOnClickListener
			}
			viewLifecycleOwner.lifecycleScope.launch {
				imageHelper.shareViaIntent(requireActivity(), currentPhotoPath!!, "Some share text:")
			}
		}
	}

	enum class PermissionOption {
		CAMERA, GALLERY
	}
}