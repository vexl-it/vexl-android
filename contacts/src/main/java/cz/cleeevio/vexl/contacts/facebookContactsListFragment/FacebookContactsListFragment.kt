package cz.cleeevio.vexl.contacts.facebookContactsListFragment

import cz.cleeevio.vexl.contacts.R
import cz.cleeevio.vexl.contacts.databinding.FragmentFacebookContactsListBinding
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.repository.model.contact.BaseContact
import lightbase.core.baseClasses.BaseFragment
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
	}

	override fun initView() {

		binding.contactsListWidget.setupListeners(
			onContactImportSwitched = { contact: BaseContact, selected: Boolean ->
//				viewModel.contactSelected(contact, selected)
			},
			onUnselectAllClicked = {
//				viewModel.unselectAll()
			}
		)

		viewModel.loadNotSyncedFacebookContacts()
	}

}