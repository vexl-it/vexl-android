package cz.cleevio.vexl.contacts.notificationFragment

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.contacts.R
import cz.cleevio.vexl.contacts.databinding.FragmentNotificationBinding
import cz.cleevio.vexl.contacts.importContactsFragment.ImportContactsFragment
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.utils.PermissionResolver
import org.koin.androidx.viewmodel.ext.android.viewModel

const val PACKAGE = "package"
//This fragment should be used only on API 33 and higher
class NotificationFragment : BaseFragment(R.layout.fragment_notification) {

	private val binding by viewBinding(FragmentNotificationBinding::bind)
	override val viewModel by viewModel<NotificationViewModel>()
	private var reallowPermissions = false

	private val requestContactsPermissions = registerForActivityResult(
		ActivityResultContracts.RequestMultiplePermissions()
	) { permissions ->
		PermissionResolver.resolve(requireActivity(), permissions,
			allGranted = {
				viewModel.updateHasReadContactPermissions(true)
				reallowPermissions = false
			},
			denied = {
				viewModel.updateHasReadContactPermissions(false)
				reallowPermissions = false
			},
			permanentlyDenied = {
				if (reallowPermissions) {
					startActivity(Intent().apply {
						action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
						data = Uri.fromParts(PACKAGE, requireContext().packageName, null)
					})
				} else {
					viewModel.updateHasReadContactPermissions(false)
				}
				reallowPermissions = false
			})
	}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.hasPermissionsEvent.collect { hasPermission ->
				if (hasPermission) {
					continueToNextScreen()
				} else {
					showPermissionDeniedDialog()
				}
			}
		}
	}

	private fun showPermissionDeniedDialog() {
		val builder = MaterialAlertDialogBuilder(requireContext())
			.setNegativeButton(R.string.import_contacts_not_allow) { dialog, _ ->
				dialog.dismiss()
				continueToNextScreen()
			}
			.setPositiveButton(R.string.import_contacts_allow) { _, _ ->
				reallowPermissions = true
				checkReadContactsPermissions()
			}
		builder.setCustomTitle(
			TextView(requireContext()).apply {
				maxLines = ImportContactsFragment.MAX_DIALOG_LINES
				setPadding(ImportContactsFragment.DIALOG_PADDING, ImportContactsFragment.DIALOG_PADDING, ImportContactsFragment.DIALOG_PADDING, ImportContactsFragment.DIALOG_PADDING)
				layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
				isSingleLine = false
				//todo: fix correct text
				text = getString(R.string.import_contacts_request_description)
				setTextAppearance(R.style.TextAppearance_Cleevio_DialogBody)
			}
		)

		builder.show()
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
		if (Build.VERSION.SDK_INT >= 33) {
			requestContactsPermissions.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
		} else {
			continueToNextScreen()
		}
	}

	override fun onResume() {
		super.onResume()

		binding.importBtn.setOnClickListener {
			checkReadContactsPermissions()
		}
	}

	private fun continueToNextScreen() {
		findNavController().safeNavigateWithTransition(
			NotificationFragmentDirections.proceedToFinishImportFragment()
		)
	}
}