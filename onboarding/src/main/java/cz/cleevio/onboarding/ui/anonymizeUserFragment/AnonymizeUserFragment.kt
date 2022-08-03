package cz.cleevio.onboarding.ui.anonymizeUserFragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.Resources
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import coil.load
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.onboarding.R
import cz.cleevio.onboarding.databinding.FragmentAnonymizeUserBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel


class AnonymizeUserFragment : BaseFragment(R.layout.fragment_anonymize_user) {

	private val binding by viewBinding(FragmentAnonymizeUserBinding::bind)
	override val viewModel by viewModel<AnonymizeUserViewModel>()

	override val hasMenu = true

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.uiState.collect {
				when (it) {
					AnonymizeUserViewModel.UIState.Normal -> {
						binding.anonymizeUserIcon.isVisible = false
						binding.continueBtn.text = getString(R.string.anonymize_user_btn)
						binding.anonymizeUserNote.text = getString(R.string.anonymize_user_note)
						binding.anonymizeUserTitle.text = getString(R.string.anonymize_user_title)
//					TODO	binding.anonymizeUserName.text = data.name
//					TODO	binding.anonymizeUserImage.setImageDrawable(data.avatar)
					}
					is AnonymizeUserViewModel.UIState.Anonymized -> {
						binding.continueBtn.isEnabled = true
						val data = it.nameWithAvatar
						binding.anonymizeUserName.text = data.name
						binding.anonymizeUserImage.setImageDrawable(data.avatar)
						binding.anonymizeUserIcon.isEnabled = true
						binding.anonymizeUserIcon.isVisible = true
						binding.anonymizeUserNote.text = getString(R.string.anonymize_user_note2)
						binding.anonymizeUserNote.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
						binding.anonymizeUserTitle.text = getString(R.string.anonymize_user_title2)
						binding.continueBtn.text = getString(R.string.general_continue)
					}
				}
			}
		}

		repeatScopeOnStart {
			viewModel.currentUser.collect {
				it ?: return@collect
				binding.anonymizeUserName.text = it.username
				binding.anonymizeUserImage.load(it.avatar)
			}
		}
	}

	override fun initView() {
		binding.close.setOnClickListener {
			findNavController().popBackStack()
		}

		binding.continueBtn.setOnClickListener {
			if (viewModel.uiState.value == AnonymizeUserViewModel.UIState.Normal) {
				binding.continueBtn.isEnabled = false
				startSlideAnimation()
				viewModel.anonymizeUser(requireContext())
			} else {
				Toast.makeText(requireContext(), "Dale", Toast.LENGTH_SHORT).show()
			}
		}

		binding.anonymizeUserIcon.setOnClickListener {
			startSlideAnimation()
			viewModel.anonymizeUser(requireContext())
			binding.anonymizeUserIcon.isEnabled = false
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}
	}

	private fun startSlideAnimation() {
		val firstPartAnimation = ObjectAnimator.ofFloat(
			binding.slideEffect,
			"x",
			Resources.getSystem().displayMetrics.widthPixels.toFloat(),
			0f
		).apply {
			duration = resources.getInteger(R.integer.anonymize_duration).toLong()
			interpolator = DecelerateInterpolator()
		}

		val secondPartAnimation = ObjectAnimator.ofFloat(
			binding.slideEffect,
			"x",
			0f,
			-Resources.getSystem().displayMetrics.widthPixels.toFloat()
		).apply {
			duration = resources.getInteger(R.integer.anonymize_duration).toLong()
			interpolator = AccelerateInterpolator()
		}

		val animationSet = AnimatorSet()
		animationSet.playSequentially(firstPartAnimation, secondPartAnimation)

		animationSet.addListener(object : Animator.AnimatorListener {
			override fun onAnimationStart(animation: Animator?) {
				binding.slideEffect.isVisible = true
			}

			override fun onAnimationEnd(animation: Animator?) {
				binding.slideEffect.isVisible = false
			}

			override fun onAnimationCancel(animation: Animator?) = Unit
			override fun onAnimationRepeat(animation: Animator?) = Unit
		})

		animationSet.start()
	}
}
