package cz.cleevio.profile.editNameFragment

import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.profile.R
import cz.cleevio.profile.databinding.FragmentEditNameBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.dpValueToPx
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditNameFragment : BaseFragment(R.layout.fragment_edit_name) {

	override val viewModel by viewModel<EditNameViewModel>()
	private val binding by viewBinding(FragmentEditNameBinding::bind)

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.wasSuccessful.collect {
				if (it) {
					findNavController().popBackStack()
				} else {
					// TODO finish later
					Toast.makeText(requireContext(), "Name edit not successful", Toast.LENGTH_SHORT)
						.show()
				}
			}
		}
	}

	override fun initView() {
		binding.editNameClose.setOnClickListener {
			findNavController().popBackStack()
		}

		binding.editNameInput.editText?.doAfterTextChanged {
			if (it?.toString()?.isBlank() == true) {
				binding.editNameInput.editText?.error = "New name cannot be blank"
			} else {
				binding.editNameInput.editText?.error = null
				viewModel.newName = it?.toString() ?: ""
			}
		}

		binding.editNameSaveBtn.setOnClickListener {
			viewModel.editName()
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME + requireContext().dpValueToPx(BOTTOM_EXTRA_PADDING).toInt()
			)
		}

	}

	private companion object {
		const val BOTTOM_EXTRA_PADDING = 40
	}
}