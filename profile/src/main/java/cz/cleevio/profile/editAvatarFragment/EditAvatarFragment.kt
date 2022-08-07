package cz.cleevio.profile.editAvatarFragment

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import cz.cleevio.core.utils.*
import cz.cleevio.profile.R
import cz.cleevio.profile.cameraFragment.PERMANENTLY_DENIED_DURATION
import cz.cleevio.profile.databinding.FragmentEditAvatarBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.extensions.openAppSettings
import cz.cleevio.vexl.lightbase.core.utils.PermissionResolver
import lightbase.camera.ui.photoBottomSheet.PhotoClickOptions
import lightbase.camera.ui.photoBottomSheet.PhotoOptionsBottomSheetDialog
import lightbase.camera.ui.takePhotoFragment.TakePhotoFragment
import lightbase.camera.ui.takePhotoFragment.TakePhotoResult
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditAvatarFragment : BaseFragment(R.layout.fragment_edit_avatar) {

	override val viewModel by viewModel<EditAvatarViewModel>()
	private val binding by viewBinding(FragmentEditAvatarBinding::bind)

	private var permissionOption: PhotoClickOptions? = null
	private var currentPhotoPath: Uri? = null

	private val pickImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		result.data?.data?.let { uri ->
			currentPhotoPath = uri
			viewModel.updateProfileUri(uri)
		}
	}

	private val requestCameraPermissions =
		registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
			PermissionResolver.resolve(
				requireActivity(),
				permissions,
				allGranted = {
					when (permissionOption) {
						PhotoClickOptions.TAKE_A_PICTURE -> {
							findNavController().safeNavigateWithTransition(
								EditAvatarFragmentDirections.proceedToTakePhotoFragment()
							)
						}
						PhotoClickOptions.PICK_FROM_PHOTO_LIBRARY -> {
							val photoGalleryIntent = viewModel.imageHelper.createGalleryIntent()
							pickImageResult.launch(photoGalleryIntent)
						}
						else -> Unit
					}
				},
				denied = {
					Toast.makeText(
						requireActivity(),
						getString(R.string.permission_camera_denied),
						Toast.LENGTH_SHORT
					).show()
				},
				permanentlyDenied = {
					when (permissionOption) {
						PhotoClickOptions.TAKE_A_PICTURE -> {
							showSnackbar(
								container = binding.container,
								message = getString(R.string.camera_error_permanent_ban_camera),
								buttonText = R.string.camera_open_settings,
								action = {
									requireActivity().openAppSettings()
								},
								duration = PERMANENTLY_DENIED_DURATION
							)
						}
						PhotoClickOptions.PICK_FROM_PHOTO_LIBRARY -> {
							showSnackbar(
								container = binding.container,
								message = getString(R.string.camera_error_permanent_ban_camera),
								buttonText = R.string.camera_open_settings,
								action = {
									requireActivity().openAppSettings()
								},
								duration = PERMANENTLY_DENIED_DURATION
							)
						}
						else -> Unit
					}
				})
		}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.wasSuccessful.collect {
				if (it) {
					findNavController().popBackStack()
				} else {
					// TODO finish later
					Toast.makeText(requireContext(), "You cannot upload empty image", Toast.LENGTH_SHORT)
						.show()
				}
			}
		}

		repeatScopeOnCreate {
			viewModel.userFlow.collect { user ->
				setAvatarPlaceholderVisible(isVisible = user?.avatar != null)

				user?.avatar?.let { avatar ->
					binding.editAvatarImage.load(avatar) {
						crossfade(true)
						fallback(R.drawable.ic_profile_avatar_placeholder)
						error(R.drawable.ic_profile_avatar_placeholder)
						placeholder(R.drawable.ic_profile_avatar_placeholder)
					}
				}
			}
		}

		repeatScopeOnStart {
			viewModel.profileImageUri.collect { profileImageUri ->
				if (profileImageUri == null && viewModel.userFlow.value != null) return@collect
				setAvatarPlaceholderVisible(profileImageUri != null)
				currentPhotoPath = profileImageUri

				profileImageUri?.let {
					binding.editAvatarImage.load(it) {
						crossfade(true)
						diskCachePolicy(CachePolicy.DISABLED)
						memoryCachePolicy(CachePolicy.DISABLED)
					}
				}
			}
		}
	}

	override fun initView() {
		binding.editAvatarClose.setOnClickListener {
			findNavController().popBackStack()
		}
		binding.editAvatarCornerIcon.setOnClickListener {
			binding.clickableWrapper.callOnClick()
		}
		binding.editAvatarImage.setOnClickListener {
			binding.clickableWrapper.callOnClick()
		}
		binding.clickableWrapper.setOnClickListener {
			findNavController().safeNavigateWithTransition(
				EditAvatarFragmentDirections.proceedToBottomSheetDialog()
			)
		}
		binding.editAvatarSaveBtn.setDebouncedOnClickListener {
			viewModel.editAvatar(requireContext().contentResolver)
		}

		setFragmentResultListener(PhotoOptionsBottomSheetDialog.RESULT_PHOTO_OPTIONS_RESULT) { _, bundle ->
			when (bundle.getSerializable(PhotoOptionsBottomSheetDialog.RESULT_PHOTO_OPTIONS_RESULT) as? PhotoClickOptions) {
				PhotoClickOptions.PICK_FROM_PHOTO_LIBRARY -> {
					permissionOption = PhotoClickOptions.PICK_FROM_PHOTO_LIBRARY
					requestCameraPermissions.launch(
						arrayOf(
							Manifest.permission.READ_EXTERNAL_STORAGE
						)
					)
				}
				PhotoClickOptions.TAKE_A_PICTURE -> {
					permissionOption = PhotoClickOptions.TAKE_A_PICTURE
					requestCameraPermissions.launch(
						arrayOf(
							Manifest.permission.CAMERA,
							Manifest.permission.READ_EXTERNAL_STORAGE
						)
					)
				}
				else -> Unit
			}
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}

		setupPhotoListener()
	}

	private fun setupPhotoListener() {
		setFragmentResultListener(TakePhotoFragment.RESULT_CAMERA_RESULT) { _, bundle ->
			viewModel.processTakingPhotoResult(TakePhotoResult.fromBundle(bundle))
		}
	}

	private fun setAvatarPlaceholderVisible(isVisible: Boolean) {
		binding.editAvatarCornerIcon.isVisible = isVisible
		binding.editAvatarMiddleIcon.isVisible = !isVisible
	}

	private companion object {
		const val BOTTOM_EXTRA_PADDING = 40
	}
}
