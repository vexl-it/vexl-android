package cz.cleevio.vexl.contacts.facebookContactsListFragment

import android.os.Build
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.vexl.contacts.R
import cz.cleevio.vexl.contacts.databinding.FragmentFacebookContactsListBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class FacebookContactsListFragment : BaseFragment(R.layout.fragment_facebook_contacts_list) {

	private val binding by viewBinding(FragmentFacebookContactsListBinding::bind)
	override val viewModel by viewModel<FacebookContactsListViewModel>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.notSyncedContacts.collect {
				binding.contactsListWidget.setupData(it)
			}
		}
		repeatScopeOnStart {
			viewModel.uploadSuccessful.collect {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					findNavController().navigate(
						FacebookContactsListFragmentDirections.proceedToNotificationFragment()
					)
				} else {
					viewModel.finishOnboardingAndNavigateToMain()
				}
			}
		}
	}

	override fun initView() {
		binding.contactsListWidget.setupListeners(
			onContactImportSwitched = { contact: BaseContact, selected: Boolean ->
				viewModel.contactSelected(contact, selected)
			},
			onDeselectAllClicked = {
				viewModel.unselectAll()
			},
			onSelectAllClicked = {
				viewModel.selectAll()
			}
		)

		binding.importContactsBtn.setOnClickListener {
			viewModel.uploadAllMissingContacts()
		}

		viewModel.loadNotSyncedFacebookContacts()
	}
}
