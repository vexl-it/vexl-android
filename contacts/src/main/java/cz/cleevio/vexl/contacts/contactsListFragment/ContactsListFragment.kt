package cz.cleevio.vexl.contacts.contactsListFragment

import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.vexl.contacts.R
import cz.cleevio.vexl.contacts.databinding.FragmentContactsListBinding
import cz.cleevio.core.model.OpenedFromScreen
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactsListFragment : BaseFragment(R.layout.fragment_contacts_list) {

	private val binding by viewBinding(FragmentContactsListBinding::bind)
	override val viewModel by viewModel<ContactsListViewModel>()

	private val args by navArgs<ContactsListFragmentArgs>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.notSyncedContacts.collect {
				binding.contactsListWidget.setupData(it)
			}
		}
		repeatScopeOnStart {
			viewModel.uploadSuccessful.collect {
				when (args.openedFromScreen) {
					OpenedFromScreen.UNKNOWN -> {
						findNavController().navigate(
							ContactsListFragmentDirections.proceedToImportFacebookContactsFragment()
						)
					}
					OpenedFromScreen.PROFILE -> {
						viewModel.navMainGraphModel.navigateToGraph(NavMainGraphModel.NavGraph.Profile)
					}
					OpenedFromScreen.ONBOARDING -> {
						findNavController().navigate(
							ContactsListFragmentDirections.proceedToImportFacebookContactsFragment()
						)
					}
				}
			}
		}
		repeatScopeOnStart {
			viewModel.progressFlow.collect { show ->
				binding.contactsListWidget.isVisible = !show
				binding.importContactsBtn.isVisible = !show

				if (show) {
					binding.progress.show()
				} else {
					binding.progress.hide()
				}
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

		viewModel.syncContacts(requireActivity().contentResolver, args.openedFromScreen)
	}

}