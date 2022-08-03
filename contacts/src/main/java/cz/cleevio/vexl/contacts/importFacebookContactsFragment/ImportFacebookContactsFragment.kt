package cz.cleevio.vexl.contacts.importFacebookContactsFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.contacts.R
import cz.cleevio.vexl.contacts.databinding.FragmentImportFacebookContactsBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel


class ImportFacebookContactsFragment : BaseFragment(R.layout.fragment_import_facebook_contacts) {

	private val binding by viewBinding(FragmentImportFacebookContactsBinding::bind)
	override val viewModel by viewModel<ImportFacebookContactsViewModel>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.facebookPermissionApproved.collect {
				findNavController().navigate(
					ImportFacebookContactsFragmentDirections.proceedToFacebookContactsListFragment()
				)
			}
		}
	}

	override fun initView() {
		binding.importFromFbBtn.setOnClickListener {
			viewModel.checkFacebookLogin(this)
		}

		binding.skipImport.setOnClickListener {
			findNavController().navigate(
				ImportFacebookContactsFragmentDirections.proceedToFinishImportFragment()
			)
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}
	}
}
