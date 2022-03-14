package cz.cleeevio.vexl.contacts.importContactsFragment

import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import coil.load
import cz.cleeevio.vexl.contacts.R
import cz.cleeevio.vexl.contacts.databinding.FragmentImportContactsBinding
import cz.cleevio.core.utils.viewBinding
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.dpValueToPx
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

const val BOTTOM_EXTRA_PADDING = 40

class ImportContactsFragment : BaseFragment(R.layout.fragment_import_contacts) {

	private val binding by viewBinding(FragmentImportContactsBinding::bind)
	override val viewModel by viewModel<ImportContactsViewModel>()

	override fun bindObservers() {
		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.user.collect { user ->
				binding.username.text = user?.username
				binding.avatarImage.load(user?.avatar)
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

		viewModel.getUserData()
	}
}