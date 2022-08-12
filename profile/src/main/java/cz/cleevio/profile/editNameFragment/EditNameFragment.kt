package cz.cleevio.profile.editNameFragment

import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.setDebouncedOnClickListener
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.profile.R
import cz.cleevio.profile.databinding.FragmentEditNameBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForIMEInset
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditNameFragment : BaseFragment(R.layout.fragment_edit_name) {

	override val viewModel by viewModel<EditNameViewModel>()
	private val binding by viewBinding(FragmentEditNameBinding::bind)

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.wasSuccessful.collect { resource ->
				if (resource.isSuccess()) {
					findNavController().popBackStack()
				} else if (resource.isError()) {
					Toast.makeText(
						requireContext(),
						resource.errorIdentification.message?.let { getString(it) }
							?: resource.errorIdentification.stringMessage ?: return@collect,
						Toast.LENGTH_SHORT
					).show()
				}
			}
		}

		repeatScopeOnStart {
			viewModel.oldName.collect {
				it ?: return@collect
				binding.editNameInput.editText?.setText(it.username)
			}
		}
	}

	override fun initView() {
		binding.editNameClose.setOnClickListener {
			findNavController().popBackStack()
		}

		binding.editNameInput.editText?.doAfterTextChanged {
			binding.editNameInput.editText?.error = if (it?.toString()?.isBlank() == true) {
				getString(R.string.error_nickname_blank)
			} else {
				null
			}
			viewModel.newName = it?.toString() ?: ""
		}

		binding.editNameSaveBtn.setDebouncedOnClickListener {
			viewModel.editName()
		}

		listenForInsets(binding.editNameInput) { insets ->
			binding.container.updatePadding(
				top = insets.top
			)
		}

		val buttonMargin = binding.editNameSaveBtn.marginBottom
		listenForIMEInset(binding.container) { inset ->
			binding.editNameSaveBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				bottomMargin = buttonMargin + inset
			}
		}
	}
}
