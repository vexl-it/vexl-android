package cz.cleevio.profile.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import cz.cleevio.core.R
import cz.cleevio.profile.databinding.WidgetProfileRowBinding
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater

class ProfileRowWidget constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

	private lateinit var binding: WidgetProfileRowBinding

	init {
		setupUI()

		val styledAttributes =
			context.theme.obtainStyledAttributes(attrs, cz.cleevio.core.R.styleable.ProfileRow, 0, 0)

		setText(binding.profileRowTitle, styledAttributes.getString(cz.cleevio.core.R.styleable.ProfileRow_title))
		setText(binding.profileRowSubtitle, styledAttributes.getString(cz.cleevio.core.R.styleable.ProfileRow_subtitle))

		binding.profileRowTitle.setTextColor(ContextCompat.getColor(context, styledAttributes.getResourceId(cz.cleevio.core.R.styleable.ProfileRow_text_color, R.color.gray_4)))

		val iconResource = styledAttributes.getResourceId(cz.cleevio.core.R.styleable.ProfileRow_icon, ICON_NOT_FOUND)
		if (iconResource != ICON_NOT_FOUND) {
			binding.profileRowIcon.setImageResource(iconResource)
		}
		binding.profileRowIcon.setColorFilter(ContextCompat.getColor(context, styledAttributes.getResourceId(cz.cleevio.core.R.styleable.ProfileRow_icon_color, R.color.gray_4)))
		binding.profileRowIcon.isVisible = iconResource != ICON_NOT_FOUND

		binding.profileRowLine.isVisible = styledAttributes.getBoolean(cz.cleevio.core.R.styleable.ProfileRow_line_visibility, false)
	}

	private fun setupUI() {
		binding = WidgetProfileRowBinding.inflate(layoutInflater, this)

		isClickable = true
		isFocusable = true
		foreground = ContextCompat.getDrawable(context, R.drawable.ripple_mask_16)
	}

	private fun setText(view: TextView, value: String?) {
		view.isVisible = value?.isNotBlank() == true
		view.text = value
	}

	fun setSubtitle(value: String) {
		setText(binding.profileRowSubtitle, value)
	}

	companion object {
		const val ICON_NOT_FOUND = -1
	}
}