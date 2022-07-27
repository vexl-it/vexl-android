package cz.cleevio.vexl.lightbase.core.baseClasses

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import cz.cleevio.vexl.lightbase.core.extensions.createProgressDialog

@Suppress("unused")
abstract class BaseActivity constructor(
	contentLayoutId: Int
) : AppCompatActivity(contentLayoutId) {

	private var progressDialogIndicator: AlertDialog? = null

	fun showProgress(visible: Boolean) {
		if (visible) {
			if (progressDialogIndicator == null) {
				progressDialogIndicator = createProgressDialog()
			}
			progressDialogIndicator?.show()
		} else {
			progressDialogIndicator?.dismiss()
		}
	}

	protected open fun setViewForBottomInset(container: View, containsBottomBar: Boolean = false) {
		ViewCompat.setOnApplyWindowInsetsListener(container) { _, insets ->
			container.updatePadding(
				bottom = insets.getInsets(
					WindowInsetsCompat.Type.navigationBars()
				).bottom + if (containsBottomBar) getNavigationBarHeight() else 0
			)
			insets
		}
	}

	protected open fun setViewForTopInset(container: View) {
		ViewCompat.setOnApplyWindowInsetsListener(container) { _, insets ->
			container.updatePadding(
				top = insets.getInsets(
					WindowInsetsCompat.Type.statusBars()
						or WindowInsetsCompat.Type.displayCutout()
				).top
			)
			insets
		}
	}

	protected open fun setViewForTopAndBottomInset(container: View, containsBottomBar: Boolean = false) {
		ViewCompat.setOnApplyWindowInsetsListener(container) { _, insets ->
			container.updatePadding(
				top = insets.getInsets(
					WindowInsetsCompat.Type.statusBars()
						or WindowInsetsCompat.Type.displayCutout()
				).top,
				bottom = insets.getInsets(
					WindowInsetsCompat.Type.navigationBars()
				).bottom + if (containsBottomBar) getNavigationBarHeight() else 0
			)
			insets
		}
	}

	private fun getNavigationBarHeight(): Int {
		val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
		return if (resourceId > 0) {
			resources.getDimensionPixelSize(resourceId)
		} else 0
	}
}
