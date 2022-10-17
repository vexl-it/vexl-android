package cz.cleevio.profile.profileFacebookContactsListFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.profile.databinding.BottomSheetDialogProfileFacebookContactsListBinding
import cz.cleevio.repository.model.contact.BaseContact
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFacebookContactsListFragment : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogProfileFacebookContactsListBinding
	private val viewModel by viewModel<ProfileFacebookContactsListViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogProfileFacebookContactsListBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initView()
		bindObservers()
	}

	override fun onStop() {
		dismiss()
		super.onStop()
	}

	private fun bindObservers() {
		repeatScopeOnStart {
			viewModel.notSyncedContacts.collect {
				binding.contactsListWidget.setupData(it)
			}
		}
		repeatScopeOnStart {
			viewModel.uploadSuccessful.collect {
				findNavController().popBackStack()
			}
		}
	}

	private fun initView() {
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