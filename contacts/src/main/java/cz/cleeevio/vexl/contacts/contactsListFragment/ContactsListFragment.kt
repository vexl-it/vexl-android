package cz.cleeevio.vexl.contacts.contactsListFragment

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
		}
		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.uploadSuccessful.collect {
				findNavController().navigate(
					ContactsListFragmentDirections.proceedToFacebookContactListFragment()
				)
			}
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

		binding.importContactsBtn.setOnClickListener {
			viewModel.uploadAllMissingContacts()
		}

		viewModel.syncContacts(requireActivity().contentResolver)
	}

}