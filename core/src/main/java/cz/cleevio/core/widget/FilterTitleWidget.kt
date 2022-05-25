package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.databinding.WidgetFilterTitleBinding
import lightbase.core.extensions.layoutInflater

class FilterTitleWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetFilterTitleBinding

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetFilterTitleBinding.inflate(layoutInflater, this)
	}
}