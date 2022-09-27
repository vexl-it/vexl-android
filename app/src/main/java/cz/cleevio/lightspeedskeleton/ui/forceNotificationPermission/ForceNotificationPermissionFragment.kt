package cz.cleevio.lightspeedskeleton.ui.forceNotificationPermission

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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.FragmentForceNotificationPermissionBinding
import cz.cleevio.vexl.contacts.importContactsFragment.ImportContactsFragment
import cz.cleevio.vexl.contacts.notificationFragment.PACKAGE
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.utils.PermissionResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForceNotificationPermissionFragment : BaseFragment(R.layout.fragment_force_notification_permission) {

	private val binding by viewBinding(FragmentForceNotificationPermissionBinding::bind)
	override val viewModel by viewModel<ForceNotificationPermissionViewModel>()
	private var reAllowPermissions = false

	private val requestNotificationsPermissions = registerForActivityResult(
		ActivityResultContracts.RequestMultiplePermissions()
	) { permissions ->
		PermissionResolver.resolve(
			requireActivity(), permissions,
			allGranted = {
				viewModel.updateHasPostNotificationsPermissions(true)
				reAllowPermissions = false
			},
			denied = {
				viewModel.updateHasPostNotificationsPermissions(false)
				reAllowPermissions = false
			},
			permanentlyDenied = {
				if (reAllowPermissions) {
					startActivity(Intent().apply {
						action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
						data = Uri.fromParts(PACKAGE, requireContext().packageName, null)
					})
				} else {
					viewModel.updateHasPostNotificationsPermissions(false)
				}
				reAllowPermissions = false
			})
	}

	private fun showPermissionDeniedDialog() {
		val builder = MaterialAlertDialogBuilder(requireContext())
			.setNegativeButton(cz.cleevio.vexl.contacts.R.string.notifications_permission_dialog_dont_allow) { dialog, _ ->
				dialog.dismiss()
			}
			.setPositiveButton(cz.cleevio.vexl.contacts.R.string.notifications_permission_dialog_allow) { _, _ ->
				reAllowPermissions = true
				checkPostNotificationsPermissions()
			}
		builder.setCustomTitle(
			TextView(requireContext()).apply {
				maxLines = ImportContactsFragment.MAX_DIALOG_LINES
				setPadding(ImportContactsFragment.DIALOG_PADDING, ImportContactsFragment.DIALOG_PADDING, ImportContactsFragment.DIALOG_PADDING, ImportContactsFragment.DIALOG_PADDING)
				layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
				isSingleLine = false
				text = getString(cz.cleevio.vexl.contacts.R.string.notifications_permission_dialog_title)
				setTextAppearance(cz.cleevio.vexl.contacts.R.style.TextAppearance_Cleevio_DialogBody)
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

		binding.forceUpdateBtn.setOnClickListener {
			//show system dialog to allow permissions
			checkPostNotificationsPermissions()
		}

		binding.close.setOnClickListener {
			continueToNextScreen()
		}
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

	override fun onResume() {
		super.onResume()

		if (!viewModel.notificationUtils.areNotificationsDisabled()) {
			continueToNextScreen()
		}
	}

	private fun checkPostNotificationsPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			requestNotificationsPermissions.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
		} else {
			continueToNextScreen()
		}
	}

	private fun continueToNextScreen() {
		lifecycleScope.launch(Dispatchers.Main) {
			viewModel.navMainGraphModel.navigateToGraph(
				NavMainGraphModel.NavGraph.Main
			)
		}
	}
}