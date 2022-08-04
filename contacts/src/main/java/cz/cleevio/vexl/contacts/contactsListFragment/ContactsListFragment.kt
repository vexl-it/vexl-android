package cz.cleevio.vexl.contacts.contactsListFragment

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleevio.core.model.OpenedFromScreen
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.vexl.contacts.R
import cz.cleevio.vexl.contacts.databinding.FragmentContactsListBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForIMEInset
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactsListFragment : BaseFragment(R.layout.fragment_contacts_list) {

	private val binding by viewBinding(FragmentContactsListBinding::bind)
	override val viewModel by viewModel<ContactsListViewModel>()

	private val args by navArgs<ContactsListFragmentArgs>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.contactsToBeShowed.collect {
				binding.contactsListWidget.setupData(it, OpenedFromScreen.ONBOARDING)
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
		binding.close.setOnClickListener {
			findNavController().popBackStack()
		}

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

		viewModel.syncContacts(requireActivity().contentResolver, args.openedFromScreen)

		listenForInsets(binding.importContactsBtn) { insets ->
			binding.container.updatePadding(
				top = insets.top
			)
		}

		val originMargin = binding.importContactsBtn.marginBottom
		listenForIMEInset(binding.container) { inset ->
			binding.importContactsBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				bottomMargin = inset + originMargin
			}
		}
	}
}
