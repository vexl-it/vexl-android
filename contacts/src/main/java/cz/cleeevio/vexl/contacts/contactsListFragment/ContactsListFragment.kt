package cz.cleeevio.vexl.contacts.contactsListFragment

import androidx.navigation.fragment.findNavController
import cz.cleeevio.vexl.contacts.R
import cz.cleeevio.vexl.contacts.databinding.FragmentContactsListBinding
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.repository.model.contact.BaseContact
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactsListFragment : BaseFragment(R.layout.fragment_contacts_list) {

	private val binding by viewBinding(FragmentContactsListBinding::bind)
	override val viewModel by viewModel<ContactsListViewModel>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.notSyncedContacts.collect {
				binding.contactsListWidget.setupData(it)
			}
		}
		repeatScopeOnStart {
			viewModel.uploadSuccessful.collect {
				findNavController().navigate(
					ContactsListFragmentDirections.proceedToImportFacebookContactsFragment()
				)
			}
		}
	}

	override fun initView() {
		binding.contactsListWidget.setupListeners(
			onContactImportSwitched = { contact: BaseContact, selected: Boolean ->
				viewModel.contactSelected(contact, selected)
			},
			onUnselectAllClicked = {
				viewModel.unselectAll()
			}
		)

		binding.importContactsBtn.setOnClickListener {
			viewModel.uploadAllMissingContacts()
		}

		viewModel.syncContacts(requireActivity().contentResolver)
	}

}