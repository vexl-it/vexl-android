package cz.cleevio.profile.profileContactsListFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.model.OpenedFromScreen
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.profile.databinding.BottomSheetDialogProfileContactsListBinding
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileContactsListFragment constructor(
	private val openedFromScreen: OpenedFromScreen
) : BottomSheetDialogFragment() {

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
		(dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
		initView()
		bindObservers()
	}

	override fun onStop() {
		dismiss()
		super.onStop()
	}

	private fun bindObservers() {
		repeatScopeOnStart {
			viewModel.contactsToBeShowed.collect {
				binding.contactsListWidget.setupData(it)
				binding.emptyContactsBtn.isInvisible = it.isNotEmpty()
				binding.backBtn.isVisible = it.isNotEmpty()
				binding.confirmBtn.isVisible = it.isNotEmpty()
			}
		}
		repeatScopeOnStart {
			viewModel.successful.collect {
				when (openedFromScreen) {
					OpenedFromScreen.PROFILE -> {
						dismiss()
					}
					else -> findNavController().popBackStack()
				}
			}
		}
		repeatScopeOnStart {
			viewModel.progressFlow.collect { show ->
				binding.contactsListWidget.isVisible = !show

				if (!show) {
					binding.emptyContactsBtn.isVisible = viewModel.contactsToBeShowed.replayCache.firstOrNull().isNullOrEmpty()
					binding.backBtn.isVisible = !viewModel.contactsToBeShowed.replayCache.firstOrNull().isNullOrEmpty()
					binding.confirmBtn.isVisible = !viewModel.contactsToBeShowed.replayCache.firstOrNull().isNullOrEmpty()
				} else {
					binding.emptyContactsBtn.isInvisible = true
					binding.backBtn.isInvisible = true
					binding.confirmBtn.isInvisible = true
				}

				if (show) {
					binding.progress.show()
				} else {
					binding.progress.hide()
				}
			}
		}
	}

	private fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				bottom = insets.bottomWithIME
			)
		}

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

		binding.emptyContactsBtn.setOnClickListener {
			dismiss()
		}

		binding.backBtn.setOnClickListener {
			dismiss()
		}

		binding.confirmBtn.setOnClickListener {
			viewModel.uploadAllMissingContacts()
		}

		viewModel.syncContacts(requireActivity().contentResolver, openedFromScreen)
	}
}
