package cz.cleevio.vexl.lightbase.core.extensions

import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import cz.cleevio.vexl.lightbase.R
import kotlin.math.roundToInt

fun createSnackbar(
	view: View? = null,
	message: String? = null,
	messageSpanned: Spanned? = null,
	icon: Int? = null,
	anchorView: View? = null,
	@StringRes buttonText: Int? = null,
	action: (() -> Unit)? = null,
	duration: Int = Snackbar.LENGTH_LONG,
	onDismissCallback: (() -> Unit)? = null
): Snackbar? {
	val snackBar = Snackbar.make(
		view ?: return null,
		message ?: messageSpanned ?: "",
		duration
	)
	if (action != null) {
		snackBar.setAction(buttonText!!) {
			action.invoke()
		}
	}
	if (icon != null) {
		val textView = snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
		val imgClose = ImageView(snackBar.context)
		val padding = snackBar.context.resources.getDimension(R.dimen.padding_snackbar_icon).roundToInt()
		imgClose.setPadding(padding, padding, padding, padding)
		imgClose.scaleType = ImageView.ScaleType.CENTER_INSIDE
		imgClose.setImageResource(icon)
		(textView.parent as SnackbarContentLayout).addView(
			imgClose,
			0,
			ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT
			)
		)
	}

	if (onDismissCallback != null) {
		snackBar.addCallback(object : Snackbar.Callback() {
			override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
				onDismissCallback.invoke()
				super.onDismissed(transientBottomBar, event)
			}
		})
	}

	anchorView?.let { snackBar.anchorView = it }
	return snackBar
}

@Suppress("unused")
fun showSnackbar(
	view: View? = null,
	message: String,
	icon: Int? = null,
	anchorView: View? = null,
	@StringRes buttonText: Int? = null,
	action: (() -> Unit)? = null,
	duration: Int = Snackbar.LENGTH_LONG,
	onDismissCallback: (() -> Unit)? = null
) {
	val snackbar = createSnackbar(
		view = view,
		message = message,
		icon = icon,
		anchorView = anchorView,
		buttonText = buttonText,
		action = action,
		duration = duration,
		onDismissCallback = onDismissCallback
	)
	snackbar?.show()
}
