package cz.cleeevio.vexl.contacts.contactsListFragment

import androidx.lifecycle.lifecycleScope
import cz.cleeevio.vexl.contacts.R
import cz.cleeevio.vexl.contacts.databinding.FragmentContactsListBinding
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.repository.model.contact.Contact
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactsListFragment : BaseFragment(R.layout.fragment_contacts_list) {

	private val binding by viewBinding(FragmentContactsListBinding::bind)
	override val viewModel by viewModel<ContactsListViewModel>()

	override fun bindObservers() {
		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.notSyncedContacts.collect {
				binding.contactsListWidget.setupData(it)
			}
			//FIXME only for debugging purposes - prints local contacts
//			viewModel.localContacts.collect {
//				binding.contactsListWidget.setupData(it)
//			}
		}
	}

	override fun initView() {
		binding.contactsListWidget.setupListeners(
			onContactImportSwitched = { contact: Contact, selected: Boolean ->
				viewModel.contactSelected(contact, selected)
			},
			onUnselectAllClicked = {
				viewModel.unselectAll()
			}
		)
	}

}