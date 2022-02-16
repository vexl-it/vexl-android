package cz.cleevio.lightspeedskeleton.ui.uiTemplatesFragment

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.FragmentUiTemplatesBinding
import lightbase.core.baseClasses.BaseFragment
import kotlin.math.roundToInt

class UITemplatesFragment : BaseFragment(R.layout.fragment_ui_templates) {

	private val binding by viewBinding(FragmentUiTemplatesBinding::bind)

	override fun bindObservers() = Unit

	override fun initView() {
		binding.showSnackbar.setOnClickListener {
			showSnackbar(binding.showSnackbar)
		}
		binding.showDialog.setOnClickListener {
			showDialog2()
		}
	}

	private fun showSnackbar(contextView: View) {
		val snackBar = Snackbar.make(
			contextView,
			"Show your snackbar",
			Snackbar.LENGTH_LONG
		)
		snackBar.setAction("Action") {
		}
		val textView =
			snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
		val imgClose = ImageView(snackBar.context)
		val padding =
			resources.getDimension(cz.cleevio.core.R.dimen.default_corner_size).roundToInt()
		imgClose.setPadding(padding, padding, padding, padding)
		imgClose.scaleType = ImageView.ScaleType.CENTER_INSIDE
		imgClose.setImageResource(cz.cleevio.core.R.drawable.ic_baseline_home_24)
		(textView.parent as SnackbarContentLayout).addView(
			imgClose,
			0,
			ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT
			)
		)
		snackBar.show()
	}

	private fun showDialog2() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle("Delete card")
			.setMessage("Are you sure you want to delete the card?")
			.setNegativeButton("Cancel") { dialog, _ ->
				dialog.dismiss()
			}
			.setPositiveButton("Continue") { _, _ ->
			}
			.show()
	}
}