package cz.cleevio.profile.joinGroupFragment

import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.setDebouncedOnClickListener
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.JoinGroupBottomSheetDialog
import cz.cleevio.profile.R
import cz.cleevio.profile.cameraFragment.CODE_LENGTH
import cz.cleevio.profile.databinding.FragmentJoinGroupCodeBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.hideKeyboard
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.extensions.showToast
import org.koin.androidx.viewmodel.ext.android.viewModel

class JoinGroupCodeFragment : BaseFragment(R.layout.fragment_join_group_code) {

	private val binding by viewBinding(FragmentJoinGroupCodeBinding::bind)
	override val viewModel by viewModel<JoinGroupCodeViewModel>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.groupLoaded.collect { group ->
				showBottomDialog(
					JoinGroupBottomSheetDialog(
						groupName = group.name,
						groupLogo = group.logoUrl ?: "",
						groupCode = group.code,
						destinationId = R.id.groupFragment
					)
				)
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME
			)
		}

		binding.groupCodeInput.doAfterTextChanged {
			binding.continueBtn.isEnabled = it.toString().length == CODE_LENGTH
		}

		binding.close.setDebouncedOnClickListener {
			findNavController().popBackStack()
		}

		binding.continueBtn.setDebouncedOnClickListener {
			binding.continueBtn.hideKeyboard()
			val rawInput = binding.groupCodeInput.text.toString()
			if (rawInput.isNotBlank() && rawInput.filter { it.isDigit() }.length == CODE_LENGTH) {
				viewModel.loadGroupByCode(rawInput)
			} else {
				showToast(getString(R.string.groups_code_not_found), Toast.LENGTH_LONG)
			}
		}
	}
}