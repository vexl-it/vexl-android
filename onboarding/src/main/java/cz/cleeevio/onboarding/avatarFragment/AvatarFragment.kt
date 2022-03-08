package cz.cleeevio.onboarding.avatarFragment

import android.net.Uri
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleeevio.onboarding.R
import cz.cleeevio.onboarding.databinding.FragmentAvatarBinding
import cz.cleeevio.onboarding.initPhoneFragment.BOTTOM_EXTRA_PADDING
import cz.cleevio.core.utils.viewBinding
import lightbase.camera.ui.takePhotoFragment.TakePhotoFragment
import lightbase.camera.ui.takePhotoFragment.TakePhotoResult
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.dpValueToPx
import lightbase.core.extensions.listenForInsets
import timber.log.Timber

class AvatarFragment : BaseFragment(R.layout.fragment_avatar) {

	private val args by navArgs<AvatarFragmentArgs>()
	private val binding by viewBinding(FragmentAvatarBinding::bind)

	override fun bindObservers() {
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

		setupPhotoListener()
	}

	private fun setupPhotoListener() {
		setFragmentResultListener(TakePhotoFragment.RESULT_CAMERA_RESULT) { _, bundle ->
			val result = TakePhotoResult.fromBundle(bundle)
			Timber.d("taking photo result $result")
			when (result) {
				is TakePhotoResult.Success -> {
					binding.avatarImage.setImageURI(Uri.parse(result.url))
				}
				is TakePhotoResult.Denied -> {
				}
				is TakePhotoResult.ManuallyClosed -> {
				}
				is TakePhotoResult.PermanentlyDenied -> {
				}
			}
		}
	}
}