package cz.cleevio.onboarding.ui.createAvatarFragment

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import coil.request.CachePolicy
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.showSnackbar
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentAvatarBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.extensions.openAppSettings
import cz.cleevio.vexl.lightbase.core.utils.PermissionResolver
import lightbase.camera.ui.photoBottomSheet.PhotoClickOptions
import lightbase.camera.ui.photoBottomSheet.PhotoOptionsBottomSheetDialog.Companion.RESULT_PHOTO_OPTIONS_RESULT
import lightbase.camera.ui.takePhotoFragment.TakePhotoFragment
import lightbase.camera.ui.takePhotoFragment.TakePhotoResult
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateAvatarFragment : BaseFragment(R.layout.fragment_avatar) {

	private val args by navArgs<CreateAvatarFragmentArgs>()
	private val binding by viewBinding(FragmentAvatarBinding::bind)
	override val viewModel by viewModel<CreateAvatarViewModel>()

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
								CreateAvatarFragmentDirections.proceedToTakePhotoFragment()
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
			viewModel.profileImageUri.collect { profileImageUri ->
				if (profileImageUri != null) {
					binding.continueBtn.text = getString(R.string.profile_edit_name_action)
				}

				setAvatarPlaceholderVisible(profileImageUri != null)
				currentPhotoPath = profileImageUri

				profileImageUri?.let {
					binding.createAvatarImage.load(it) {
						crossfade(true)
						diskCachePolicy(CachePolicy.DISABLED)
						memoryCachePolicy(CachePolicy.DISABLED)
					}
				}
			}
		}
	}

	override fun initView() {
		binding.close.setOnClickListener {
			findNavController().popBackStack()
		}

		binding.avatarTitle.text = getString(R.string.user_avatar_title, args.username)

		binding.createAvatarImage.setOnClickListener {
			findNavController().safeNavigateWithTransition(
				CreateAvatarFragmentDirections.proceedToBottomSheetDialog()
			)
		}

		setFragmentResultListener(RESULT_PHOTO_OPTIONS_RESULT) { _, bundle ->
			when (bundle.getSerializable(RESULT_PHOTO_OPTIONS_RESULT) as? PhotoClickOptions) {
				PhotoClickOptions.PICK_FROM_PHOTO_LIBRARY -> {
					permissionOption = PhotoClickOptions.PICK_FROM_PHOTO_LIBRARY
					requestCameraPermissions.launch(
						arrayOf(
							if (Build.VERSION.SDK_INT >= 33) {
								Manifest.permission.READ_MEDIA_IMAGES
							} else {
								Manifest.permission.READ_EXTERNAL_STORAGE
							}
						)
					)
				}
				PhotoClickOptions.TAKE_A_PICTURE -> {
					permissionOption = PhotoClickOptions.TAKE_A_PICTURE
					requestCameraPermissions.launch(
						arrayOf(
							Manifest.permission.CAMERA,
							if (Build.VERSION.SDK_INT >= 33) {
								Manifest.permission.READ_MEDIA_IMAGES
							} else {
								Manifest.permission.READ_EXTERNAL_STORAGE
							}
						)
					)
				}
				else -> Unit
			}
		}

		binding.continueBtn.setOnClickListener {
			val avatarUri = viewModel.profileImageUri.value?.toString()
			findNavController().safeNavigateWithTransition(
				CreateAvatarFragmentDirections.proceedToAnonymizeUser(
					username = args.username,
					avatarUri = avatarUri
				)
			)
		}

		setupPhotoListener()

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}
	}

	private fun setupPhotoListener() {
		setFragmentResultListener(TakePhotoFragment.RESULT_CAMERA_RESULT) { _, bundle ->
			viewModel.processTakingPhotoResult(TakePhotoResult.fromBundle(bundle))
		}
	}

	private fun setAvatarPlaceholderVisible(isVisible: Boolean) {
		binding.createAvatarCornerIcon.isVisible = isVisible
		binding.createAvatarMiddleIcon.isVisible = !isVisible
	}

	companion object {
		const val PERMANENTLY_DENIED_DURATION = 5000
	}
}
