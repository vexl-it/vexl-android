package cz.cleevio.profile.joinGroupFragment

import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.utils.setDebouncedOnClickListener
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.JoinGroupBottomSheetDialog
import cz.cleevio.profile.R
import cz.cleevio.profile.cameraFragment.CODE_LENGTH
import cz.cleevio.profile.cameraFragment.DEBUG_LOGO
import cz.cleevio.profile.databinding.FragmentJoinGroupCodeBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import lightbase.core.extensions.showToast
import org.koin.androidx.viewmodel.ext.android.viewModel

class JoinGroupCodeFragment : BaseFragment(R.layout.fragment_join_group_code) {

	private val binding by viewBinding(FragmentJoinGroupCodeBinding::bind)
	override val viewModel by viewModel<JoinGroupCodeViewModel>()

	override fun bindObservers() = Unit

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME
			)
		}

		binding.close.setDebouncedOnClickListener {
			findNavController().popBackStack()
		}

		binding.continueBtn.setDebouncedOnClickListener {
			val rawInput = binding.groupCodeInput.text.toString()
			if (rawInput.isNotBlank() && rawInput.filter { it.isDigit() }.length == CODE_LENGTH) {
				showBottomDialog(
					JoinGroupBottomSheetDialog(
						groupName = "TODO: $rawInput",
						groupLogo = DEBUG_LOGO,
						groupCode = rawInput.toLong(),
					)
				)
			} else {
				showToast(getString(R.string.groups_code_not_found), Toast.LENGTH_LONG)
			}
		}
	}

	private fun showBottomDialog(dialog: BottomSheetDialogFragment) {
		dialog.show(childFragmentManager, dialog.javaClass.simpleName)
	}
}