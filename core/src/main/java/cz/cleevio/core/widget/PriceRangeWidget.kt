package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetPriceRangeBinding
import cz.cleevio.core.model.Currency
import cz.cleevio.core.model.PriceRangeValue
import cz.cleevio.core.utils.formatCurrency
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater

class PriceRangeWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetPriceRangeBinding

	var currentCurrency = Currency.USD
	var bottomLimit: Float = 0.0f
	var topLimit: Float = 0.0f

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetPriceRangeBinding.inflate(layoutInflater, this)

		binding.priceRangeSlider.setCustomThumbDrawable(R.drawable.ic_picker)
		val initValues = binding.priceRangeSlider.values
		processLimits(initValues[0], initValues[1], currentCurrency)
		binding.priceRangeSlider.addOnChangeListener { slider, _, _ ->
			val currentValues = slider.values
			processLimits(currentValues[0], currentValues[1], currentCurrency)
		}
	}

	fun setupWithCurrency(currency: Currency) {
		currentCurrency = currency
		when (currency) {
			Currency.CZK -> {
				processLimits(BOTTOM_LIMIT_CZK.toFloat(), TOP_LIMIT_CZK.toFloat(), currency)
				binding.priceRangeSlider.valueFrom = BOTTOM_LIMIT_CZK.toFloat()
				binding.priceRangeSlider.valueTo = TOP_LIMIT_CZK.toFloat()
				binding.priceRangeSlider.setValues(BOTTOM_LIMIT_CZK.toFloat(), TOP_LIMIT_CZK.toFloat())
			}
			Currency.USD -> {
				processLimits(BOTTOM_LIMIT_USD.toFloat(), TOP_LIMIT_USD.toFloat(), currency)
				binding.priceRangeSlider.valueFrom = BOTTOM_LIMIT_USD.toFloat()
				binding.priceRangeSlider.valueTo = TOP_LIMIT_USD.toFloat()
				binding.priceRangeSlider.setValues(BOTTOM_LIMIT_USD.toFloat(), TOP_LIMIT_USD.toFloat())
			}
			Currency.EUR -> {
				processLimits(BOTTOM_LIMIT_EUR.toFloat(), TOP_LIMIT_EUR.toFloat(), currency)
				binding.priceRangeSlider.valueFrom = BOTTOM_LIMIT_EUR.toFloat()
				binding.priceRangeSlider.valueTo = TOP_LIMIT_EUR.toFloat()
				binding.priceRangeSlider.setValues(BOTTOM_LIMIT_EUR.toFloat(), TOP_LIMIT_EUR.toFloat())
			}
		}
	}

	private fun processLimits(bottomLimit: Float, topLimit: Float, currency: Currency) {
		this.bottomLimit = bottomLimit
		this.topLimit = topLimit
		binding.rangeText.text = resources.getString(
			R.string.price_range,
			bottomLimit.toInt().formatCurrency(currency.name, resources.configuration.locale),
			topLimit.toInt().formatCurrency(currency.name, resources.configuration.locale)
		)
	}

	fun getPriceRangeValue(): PriceRangeValue = PriceRangeValue(
		topLimit = topLimit,
		bottomLimit = bottomLimit
	)

	fun reset() {
		val initValues = resources.getIntArray(R.array.initial_slider_values).map { it.toFloat() }
		binding.priceRangeSlider.values = initValues
		processLimits(initValues[0], initValues[1], currentCurrency)
	}

	fun setValues(bottomLimit: Float, topLimit: Float) {
		processLimits(bottomLimit = bottomLimit, topLimit = topLimit, currentCurrency)
		binding.priceRangeSlider.setValues(bottomLimit, topLimit)
	}

	private companion object {
		private const val BOTTOM_LIMIT_CZK = 0L
		private const val TOP_LIMIT_CZK = 250_000L
		private const val BOTTOM_LIMIT_EUR = 0L
		private const val TOP_LIMIT_EUR = 10_000L
		private const val BOTTOM_LIMIT_USD = 0L
		private const val TOP_LIMIT_USD = 10_000L
	}
}
