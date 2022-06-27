package cz.cleevio.core.utils

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import cz.cleevio.core.R


object ChipViewUtils {

	fun generateChipView(
		context: Context,
		filter: String? = null,
		@DrawableRes icon: Int? = null,
		iconAtStart: Boolean = true,
		listener: () -> (Unit) = {}
	): Chip {

		val newChip = Chip(context)
		val chipDrawable = ChipDrawable.createFromAttributes(
			context,
			null,
			0,
			R.style.Widget_Cleevio_Vexl_Marketplace_FilterChip
		)
		newChip.setChipDrawable(chipDrawable)

		newChip.id = View.generateViewId()
		newChip.setOnClickListener {
			listener()
		}

		filter?.let {
			newChip.setTextAppearance(R.style.TextAppearance_Vexl_Marketplace_FilterChip)
			newChip.text = filter
		} ?: run {
			newChip.textStartPadding = 0.0f
			newChip.textEndPadding = 0.0f
		}
		icon?.let {
			val icon = getDrawable(context, icon)
			icon?.setTint(getColor(context, R.color.gray_3))
			newChip.chipIcon = icon
			newChip.isChipIconVisible = true
			newChip.layoutDirection = if (iconAtStart) {
				View.LAYOUT_DIRECTION_LTR
			} else {
				View.LAYOUT_DIRECTION_RTL
			}
		}
		return newChip
	}
}