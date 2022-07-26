package cz.cleevio.profile.editAvatarFragment

import android.net.Uri
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.setDebouncedOnClickListener
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.profile.R
import cz.cleevio.profile.databinding.FragmentEditAvatarBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.dpValueToPx
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import kotlinx.coroutines.launch
import lightbase.camera.ui.takePhotoFragment.TakePhotoFragment
import lightbase.camera.ui.takePhotoFragment.TakePhotoResult
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditAvatarFragment : BaseFragment(R.layout.fragment_edit_avatar) {

	override val viewModel by viewModel<EditAvatarViewModel>()
	private val binding by viewBinding(FragmentEditAvatarBinding::bind)

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

		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.userFlow.collect { user ->
				setAvatarPlaceholderVisible(isVisible = (user?.avatar == null) && (viewModel.profileImageUri.value == null))
				user?.avatar?.let {
					binding.editAvatarImage.setImageURI(Uri.parse(it))
				}
			}
		}

		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.profileImageUri.collect { profileImageUri ->
				setAvatarPlaceholderVisible(isVisible = profileImageUri == null)
				binding.editAvatarImage.setImageURI(profileImageUri)
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
			lifecycleScope.launchWhenResumed {
				findNavController().navigate(
					EditAvatarFragmentDirections.proceedToTakePhotoFragment()
				)
			}
		}
		binding.editAvatarSaveBtn.setDebouncedOnClickListener {
			viewModel.editAvatar(requireContext().contentResolver)
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME + requireContext().dpValueToPx(BOTTOM_EXTRA_PADDING).toInt()
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
		binding.editAvatarCornerIcon.isVisible = !isVisible
		binding.editAvatarMiddleIcon.isVisible = isVisible
	}

	private companion object {
		const val BOTTOM_EXTRA_PADDING = 40
	}
}