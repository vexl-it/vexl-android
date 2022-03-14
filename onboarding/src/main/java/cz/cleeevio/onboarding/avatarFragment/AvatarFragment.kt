package cz.cleeevio.onboarding.avatarFragment

import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleeevio.onboarding.R
import cz.cleeevio.onboarding.databinding.FragmentAvatarBinding
import cz.cleeevio.onboarding.initPhoneFragment.BOTTOM_EXTRA_PADDING
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.viewBinding
import kotlinx.coroutines.launch
import lightbase.camera.ui.takePhotoFragment.TakePhotoFragment
import lightbase.camera.ui.takePhotoFragment.TakePhotoResult
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.dpValueToPx
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class AvatarFragment : BaseFragment(R.layout.fragment_avatar) {

	private val args by navArgs<AvatarFragmentArgs>()
	private val binding by viewBinding(FragmentAvatarBinding::bind)
	override val viewModel by viewModel<AvatarViewModel>()

	override fun bindObservers() {
		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.profileImageUri.collect { profileImageUri ->
				binding.avatarImage.setImageURI(profileImageUri)
			}
		}

		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.userRegistration.collect {
				it?.let {
					viewModel.navMainGraphModel.navigateToGraph(
						NavMainGraphModel.NavGraph.Contacts
					)
				}
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME + requireContext().dpValueToPx(BOTTOM_EXTRA_PADDING).toInt()
			)
		}

		binding.avatarTitle.text = getString(R.string.avatar_welcome, args.username)

		binding.avatarImage.setOnClickListener {
			findNavController().navigate(
				AvatarFragmentDirections.proceedToTakePhotoFragment()
			)
		}

		binding.continueBtn.setOnClickListener {
			viewModel.registerUser(args.username, requireContext().contentResolver)
		}

		setupPhotoListener()
	}

	private fun setupPhotoListener() {
		setFragmentResultListener(TakePhotoFragment.RESULT_CAMERA_RESULT) { _, bundle ->
			viewModel.processTakingPhotoResult(TakePhotoResult.fromBundle(bundle))
		}
	}
}