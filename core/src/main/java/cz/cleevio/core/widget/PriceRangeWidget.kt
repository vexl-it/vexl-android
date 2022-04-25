package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetPriceRangeBinding
import cz.cleevio.core.model.PriceRangeValue
import lightbase.core.extensions.layoutInflater

class PriceRangeWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetPriceRangeBinding

	var bottomLimit: Float = 0.0f
	var topLimit: Float = 0.0f

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetPriceRangeBinding.inflate(layoutInflater, this)

		val initValues = binding.priceRangeSlider.values
		processLimits(initValues[0], initValues[1])
		binding.priceRangeSlider.addOnChangeListener { slider, _, _ ->
			val currentValues = slider.values
			processLimits(currentValues[0], currentValues[1])
		}
	}

	private fun processLimits(bottomLimit: Float, topLimit: Float) {
		this.bottomLimit = bottomLimit
		this.topLimit = topLimit
		binding.rangeText.text = resources.getString(R.string.price_range, bottomLimit, topLimit)
	}

	fun getPriceRangeValue(): PriceRangeValue = PriceRangeValue(
		topLimit = topLimit,
		bottomLimit = bottomLimit
	)
}