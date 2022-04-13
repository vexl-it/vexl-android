package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetPriceRangeBinding
import lightbase.core.extensions.layoutInflater
import org.koin.core.component.KoinComponent

class PriceRangeWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

	private lateinit var binding: WidgetPriceRangeBinding

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetPriceRangeBinding.inflate(layoutInflater, this)

		val initValues = binding.priceRangeSlider.values
		showLimits(initValues[0], initValues[1])
		binding.priceRangeSlider.addOnChangeListener { slider, _, _ ->
			val currentValues = slider.values
			showLimits(currentValues[0], currentValues[1])
		}
	}

	private fun showLimits(bottomLimit: Float, topLimit: Float) {
		binding.rangeText.text = resources.getString(R.string.price_range, bottomLimit, topLimit)
	}
}