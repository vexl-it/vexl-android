package cz.cleeevio.vexl.contacts.importContactsFragment

import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.cleeevio.vexl.contacts.R
import cz.cleeevio.vexl.contacts.databinding.FragmentImportContactsBinding
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.dpValueToPx
import lightbase.core.extensions.listenForInsets
import lightbase.core.utils.PermissionResolver
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val BOTTOM_EXTRA_PADDING = 40

class ImportContactsFragment : BaseFragment(R.layout.fragment_import_contacts) {

	private val binding by viewBinding(FragmentImportContactsBinding::bind)
	override val viewModel by viewModel<ImportContactsViewModel>()

	private val requestContactsPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
		PermissionResolver.resolve(requireActivity(), permissions,
			allGranted = {
				viewModel.updateHasReadContactPermissions(true)
				viewModel.syncContacts(requireActivity().contentResolver)
			},
			denied = {
				viewModel.updateHasReadContactPermissions(false)
			},
			permanentlyDenied = {
				viewModel.updateHasReadContactPermissions(false)
			})
	}

	override fun bindObservers() {
		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.user.collect { user ->
				binding.username.text = user?.username
				binding.avatarImage.load(user?.avatar)
			}
		}

		repeatScopeOnStart {
			viewModel.hasPermissionsEvent.collect { hasPermisson ->
				if (hasPermisson) {
					Timber.tag("ASDX").d("Permission granted")
					findNavController().navigate(
						ImportContactsFragmentDirections.proceedToContactsListFragment()
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
				bottom = insets.bottomWithIME + requireContext().dpValueToPx(BOTTOM_EXTRA_PADDING).toInt()
			)
		}
	}

	private fun checkReadContactsPermissions() {
		requestContactsPermissions.launch(arrayOf(Manifest.permission.READ_CONTACTS))
	}

	override fun onResume() {
		super.onResume()

		binding.importContactsBtn.setOnClickListener {
			checkReadContactsPermissions()
		}
	}

	private fun showPermissionDeniedDialog() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.import_contacts_request_title)
			.setMessage(R.string.import_contacts_request_description)
			.setNegativeButton(R.string.import_contacts_not_allow) { dialog, _ ->
				dialog.dismiss()
			}
			.setPositiveButton(R.string.import_contacts_allow) { _, _ ->
				checkReadContactsPermissions()
			}
			.show()
	}
}