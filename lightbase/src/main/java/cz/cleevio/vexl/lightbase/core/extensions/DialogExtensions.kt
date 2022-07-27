package cz.cleevio.vexl.lightbase.core.extensions

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.cleevio.vexl.lightbase.R

@Suppress("unused")
fun Context.showDialog(
	@StringRes title: Int? = null,
	@StringRes message: Int? = null,
	@StringRes positiveButtonText: Int? = null,
	@StringRes negativeButtonText: Int? = null,
	@StringRes neutralButtonText: Int? = null,
	onPositive: () -> Unit = {},
	onNegative: () -> Unit = {},
	onNeutral: () -> Unit = {}
) {
	MaterialAlertDialogBuilder(this).apply {
		title?.let { setTitle(it) }
		message?.let { setMessage(it) }
		positiveButtonText?.let {
			setPositiveButton(it) { _, _ ->
				onPositive.invoke()
			}
		}
		negativeButtonText?.let {
			setNegativeButton(it) { dialog, _ ->
				onNegative.invoke()
				dialog.dismiss()
			}
		}
		neutralButtonText?.let {
			setNeutralButton(it) { _, _ ->
				onNeutral.invoke()
			}
		}
		show()
	}
}

fun Context.createProgressDialog(isCancellable: Boolean = false): AlertDialog {
	return MaterialAlertDialogBuilder(this)
		.setView(R.layout.fragment_progress_dialog)
		.setBackground(ColorDrawable(Color.TRANSPARENT))
		.setCancelable(isCancellable)
		.create()
}
