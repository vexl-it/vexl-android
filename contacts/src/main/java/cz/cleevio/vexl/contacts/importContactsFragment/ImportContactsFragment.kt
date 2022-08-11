package cz.cleevio.vexl.contacts.importContactsFragment

import android.Manifest
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.cleevio.core.model.OpenedFromScreen
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.setEnterTransitionSlideToLeft
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.contacts.R
import cz.cleevio.vexl.contacts.databinding.FragmentImportContactsBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.utils.PermissionResolver
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ImportContactsFragment : BaseFragment(R.layout.fragment_import_contacts) {

	private val binding by viewBinding(FragmentImportContactsBinding::bind)
	override val viewModel by viewModel<ImportContactsViewModel>()

	private val requestContactsPermissions = registerForActivityResult(
		ActivityResultContracts.RequestMultiplePermissions()
	) { permissions ->
		PermissionResolver.resolve(requireActivity(), permissions,
			allGranted = {
				viewModel.updateHasReadContactPermissions(true)
			},
			denied = {
				viewModel.updateHasReadContactPermissions(false)
			},
			permanentlyDenied = {
				viewModel.updateHasReadContactPermissions(false)
			})
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setEnterTransitionSlideToLeft()
	}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.hasPermissionsEvent.collect { hasPermission ->
				if (hasPermission) {
					Timber.tag("ASDX").d("Permission granted")
					findNavController().safeNavigateWithTransition(
						ImportContactsFragmentDirections.proceedToContactsListFragment(
							openedFromScreen = OpenedFromScreen.ONBOARDING
						)
					)
				} else {
					Timber.tag("ASDX").d("Permission rejected")
					showPermissionDeniedDialog()
				}
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}
	}

	private fun checkReadContactsPermissions() {
		requestContactsPermissions.launch(arrayOf(Manifest.permission.READ_CONTACTS))
	}

	override fun onResume() {
		super.onResume()

		binding.importBtn.setOnClickListener {
			checkReadContactsPermissions()
		}
	}

	private fun showPermissionDeniedDialog() {
		val builder = MaterialAlertDialogBuilder(requireContext())
			.setNegativeButton(R.string.import_contacts_not_allow) { dialog, _ ->
				dialog.dismiss()
			}
			.setPositiveButton(R.string.import_contacts_allow) { _, _ ->
				checkReadContactsPermissions()
			}
		builder.setCustomTitle(
			TextView(requireContext()).apply {
				maxLines = MAX_DIALOG_LINES
				setPadding(DIALOG_PADDING, DIALOG_PADDING, DIALOG_PADDING, DIALOG_PADDING)
				layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
				isSingleLine = false
				text = getString(R.string.import_contacts_request_description)
				setTextAppearance(R.style.TextAppearance_Cleevio_DialogBody)
			}
		)

		builder.show()
	}

	companion object {
		const val DIALOG_PADDING = 30
		const val MAX_DIALOG_LINES = 3
	}
}
