package cz.cleevio.onboarding.ui.avatarFragment

import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentAvatarBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import lightbase.camera.ui.takePhotoFragment.TakePhotoFragment
import lightbase.camera.ui.takePhotoFragment.TakePhotoResult
import org.koin.androidx.viewmodel.ext.android.viewModel

class AvatarFragment : BaseFragment(R.layout.fragment_avatar) {

	private val args by navArgs<AvatarFragmentArgs>()
	private val binding by viewBinding(FragmentAvatarBinding::bind)
	override val viewModel by viewModel<AvatarViewModel>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.profileImageUri.collect { profileImageUri ->
				setAvatarPlaceholderVisible(profileImageUri != null)
				binding.createAvatarImage.setImageURI(profileImageUri)
				binding.continueBtn.isEnabled = profileImageUri != null
			}
		}

		repeatScopeOnStart {
			viewModel.user.collect {
				it?.let {
					findNavController().safeNavigateWithTransition(
						AvatarFragmentDirections.proceedToAnonymizeUser()
					)
				}
			}
		}

		repeatScopeOnStart {
			viewModel.loading.collect { loading ->
				binding.continueBtn.isEnabled = !loading
				binding.progressbar.isVisible = loading
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
				AvatarFragmentDirections.proceedToTakePhotoFragment()
			)
		}

		binding.continueBtn.setOnClickListener {
			viewModel.registerUser(args.username, requireContext().contentResolver)
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
}
