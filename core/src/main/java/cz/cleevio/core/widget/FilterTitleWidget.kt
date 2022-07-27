package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.databinding.WidgetFilterTitleBinding
import cz.cleevio.core.model.OfferType
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater

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

	fun setTypeAndTitle(type: OfferType, title: String) {
		binding.type.text = type.name
		binding.title.text = title
	}

	fun setListeners(onReset: () -> Unit, onClose: () -> Unit) {
		binding.reset.setOnClickListener {
			onReset()
		}

		binding.close.setOnClickListener {
			onClose()
		}
	}
}