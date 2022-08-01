package cz.cleevio.vexl.contacts.facebookContactsListFragment

import androidx.navigation.fragment.findNavController
import cz.cleevio.core.model.OpenedFromScreen
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
				binding.contactsListWidget.setupData(it, OpenedFromScreen.ONBOARDING)
			}
		}
		repeatScopeOnStart {
			viewModel.uploadSuccessful.collect {
				findNavController().navigate(
					FacebookContactsListFragmentDirections.proceedToFinishImportFragment()
				)
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
			}
		)

		binding.importContactsBtn.setOnClickListener {
			viewModel.uploadAllMissingContacts()
		}

		viewModel.loadNotSyncedFacebookContacts()
	}

}