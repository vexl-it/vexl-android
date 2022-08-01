package cz.cleevio.profile.profileFacebookContactsListFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogJoinGroupBinding
import cz.cleevio.core.model.OpenedFromScreen
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.profile.databinding.BottomSheetDialogProfileFacebookContactsListBinding
import cz.cleevio.profile.databinding.FragmentProfileBinding
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

	private fun bindObservers() {
		repeatScopeOnStart {
			viewModel.notSyncedContacts.collect {
				binding.contactsListWidget.setupData(it, openedFromScreen = OpenedFromScreen.PROFILE)
			}
		}
		repeatScopeOnStart {
			viewModel.uploadSuccessful.collect {
				// fixme: navigate as expected
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
				viewModel.selectAll()
			}
		)

		binding.importContactsBtn.setOnClickListener {
			viewModel.uploadAllMissingContacts()
		}

		viewModel.loadNotSyncedFacebookContacts()
	}

}