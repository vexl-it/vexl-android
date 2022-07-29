package cz.cleevio.profile.profileContactsListFragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.cleevio.core.model.OpenedFromScreen
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.profile.databinding.BottomSheetDialogProfileContactsListBinding
import cz.cleevio.profile.databinding.FragmentProfileBinding
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.vexl.lightbase.core.utils.PermissionResolver
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ProfileContactsListFragment(val openedFromScreen: OpenedFromScreen) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogProfileContactsListBinding
	private val viewModel by viewModel<ProfileContactsListViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogProfileContactsListBinding.inflate(layoutInflater, container, false)
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
				binding.contactsListWidget.setupData(it)
			}
		}
		repeatScopeOnStart {
			viewModel.uploadSuccessful.collect {
				when (openedFromScreen) {
					OpenedFromScreen.PROFILE -> {
						viewModel.navMainGraphModel.navigateToGraph(NavMainGraphModel.NavGraph.Profile)
					}
					else -> findNavController().popBackStack()
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

	private fun initView() {
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

		viewModel.syncContacts(requireActivity().contentResolver, openedFromScreen)
	}


}