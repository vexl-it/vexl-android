package cz.cleevio.vexl.lightbase.core.baseClasses

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.vexl.lightbase.core.extensions.setDarkStatusIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseBottomSheetDialog constructor(
	val isTopInset: Boolean = true,
	val isBottomInset: Boolean = true
) : BottomSheetDialogFragment() {

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		lifecycleScope.launch {
			delay(50)
			setDarkStatusIcons(dialog!!)
		}

		dialog?.findViewById<View>(com.google.android.material.R.id.container)?.fitsSystemWindows = false
		val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
		if (bottomSheet != null) {
			ViewCompat.setOnApplyWindowInsetsListener(bottomSheet) { v, insets ->
				val topInset = if (isTopInset) {
					insets.getInsets(
						WindowInsetsCompat.Type.statusBars()
							or WindowInsetsCompat.Type.displayCutout()
					).top
				} else 0

				val bottomInset = if (isBottomInset) {
					insets.getInsets(
						WindowInsetsCompat.Type.navigationBars()
					).bottom
				} else 0

				v.updatePadding(
					top = topInset,
					bottom = bottomInset
				)
				insets
			}
		}
	}
}
