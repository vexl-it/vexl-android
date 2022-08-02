package cz.cleevio.core.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import cz.cleevio.core.R
import cz.cleevio.vexl.lightbase.core.extensions.dpValueToPx

class SegmentedProgressBar @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

	private var activeSegments = 1
	private var size = 1
	private var activeColor: Int = 0
	private var inactiveColor: Int = 0

	init {
		val styledAttributes = context.theme.obtainStyledAttributes(attrs, R.styleable.SegmentedProgressBar, 0, 0)
		size = styledAttributes.getInteger(R.styleable.SegmentedProgressBar_size, 1)
		activeSegments = styledAttributes.getInteger(R.styleable.SegmentedProgressBar_active_count, 1)
		activeColor = styledAttributes.getColor(R.styleable.SegmentedProgressBar_active_color, 0)
		inactiveColor = styledAttributes.getColor(R.styleable.SegmentedProgressBar_inactive_color, 0)
		styledAttributes.recycle()

		setupUI()
	}

	fun setupUI() {
		orientation = HORIZONTAL

		setupBars()
	}

	private fun setupBars() {
		removeAllViews()

		repeat(size) { index ->
			val view = View(context)
			view.setBackgroundResource(R.drawable.background_segmented_progress_item)
			view.backgroundTintList = ColorStateList.valueOf(inactiveColor)

			if (index < activeSegments) {
				view.backgroundTintList = ColorStateList.valueOf(activeColor)
			}

			view.layoutParams = LayoutParams(0, context.dpValueToPx(4).toInt()).apply {
				weight = 1f
				marginStart = context.dpValueToPx(2).toInt()
				marginEnd = context.dpValueToPx(2).toInt()

				when (index) {
					0 -> marginStart = 0
					size - 1 -> marginEnd = 0
				}
			}

			addView(view)
		}
	}

	fun setProgress(progress: Int) {
		activeSegments = progress
		setupBars()
	}
}
