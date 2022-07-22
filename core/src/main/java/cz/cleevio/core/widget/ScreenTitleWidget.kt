package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.databinding.WidgetCreateTitleBinding
import lightbase.core.extensions.layoutInflater

class ScreenTitleWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetCreateTitleBinding

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetCreateTitleBinding.inflate(layoutInflater, this)
	}

	fun setTypeAndTitle(title: String) {
		binding.title.text = title
	}

	fun setListeners(onClose: () -> Unit) {
		binding.close.setOnClickListener {
			onClose()
		}
	}
}