package cz.cleeevio.vexl.contacts.importFacebookContactsFragment

import androidx.navigation.fragment.findNavController
import coil.load
import cz.cleeevio.vexl.contacts.R
import cz.cleeevio.vexl.contacts.databinding.FragmentImportFacebookContactsBinding
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class ImportFacebookContactsFragment : BaseFragment(R.layout.fragment_import_facebook_contacts) {

	private val binding by viewBinding(FragmentImportFacebookContactsBinding::bind)
	override val viewModel by viewModel<ImportFacebookContactsViewModel>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.user.collect { user ->
				binding.username.text = user?.username
				binding.avatarImage.load(user?.avatar) {
					crossfade(true)
					fallback(R.drawable.ic_baseline_person_128)
					error(R.drawable.ic_baseline_person_128)
					placeholder(R.drawable.ic_baseline_person_128)
				}
			}
		}
		repeatScopeOnStart {
			viewModel.facebookPermissionApproved.collect {
				findNavController().navigate(
					ImportFacebookContactsFragmentDirections.proceedToFacebookContactsListFragment()
				)
			}
		}
	}

	override fun initView() {
		binding.importContactsBtn.setOnClickListener {
			viewModel.checkFacebookLogin(this)
		}
		binding.importContactsSkipBtn.setOnClickListener {
			findNavController().navigate(
				ImportFacebookContactsFragmentDirections.proceedToFacebookContactsListFragment() // TODO skip FB
			)
		}
	}

}