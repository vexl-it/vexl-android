package cz.cleevio.core.widget

import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetPriceRangeBinding
import cz.cleevio.core.model.PriceRangeValue
import cz.cleevio.repository.model.Currency
import cz.cleevio.repository.model.Currency.Companion.getCurrencySymbol
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import timber.log.Timber

const val DEBOUNCE_DELAY = 1200L

class PriceRangeWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetPriceRangeBinding
	private val coroutineScope = CoroutineScope(Dispatchers.Main)

	var currentCurrency = Currency.USD
	var bottomLimit: Float = 0.0f
	var topLimit: Float = 0.0f

	private val _newValues = MutableSharedFlow<Pair<Float, Float>>(replay = 1)

	@OptIn(FlowPreview::class)
	val newValues = _newValues.asSharedFlow()
		.debounce(DEBOUNCE_DELAY)

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

		binding.minInputValue.doAfterTextChanged {
			try {
				_newValues.tryEmit(
					Pair(
						it.toString().toFloat(),
						topLimit
					)
				)
			} catch (e: NumberFormatException) {
				Timber.w(e)
			}
		}

		binding.maxInputValue.doAfterTextChanged {
			try {
				_newValues.tryEmit(
					Pair(
						bottomLimit,
						it.toString().toFloat()
					)
				)
			} catch (e: NumberFormatException) {
				Timber.w(e)
			}
		}

		coroutineScope.launch {
			newValues.collect {
				processLimits(
					bottomLimit = it.first,
					topLimit = it.second,
					currency = currentCurrency
				)
			}
		}
	}

	fun setupWithCurrency(currency: Currency) {
		currentCurrency = currency
		when (currency) {
			Currency.CZK -> {
				binding.priceRangeSlider.valueFrom = BOTTOM_LIMIT_CZK.toFloat()
				binding.priceRangeSlider.valueTo = TOP_LIMIT_CZK.toFloat()
				binding.maxInputValue.filters = arrayOf<InputFilter>(
					MinMaxFilter(BOTTOM_LIMIT_CZK.toFloat(), TOP_LIMIT_CZK.toFloat())
				)
				processLimits(BOTTOM_LIMIT_CZK.toFloat(), TOP_LIMIT_CZK.toFloat(), currency)
			}
			Currency.USD -> {
				binding.priceRangeSlider.valueFrom = BOTTOM_LIMIT_USD.toFloat()
				binding.priceRangeSlider.valueTo = TOP_LIMIT_USD.toFloat()
				binding.maxInputValue.filters = arrayOf<InputFilter>(
					MinMaxFilter(BOTTOM_LIMIT_USD.toFloat(), TOP_LIMIT_USD.toFloat())
				)
				processLimits(BOTTOM_LIMIT_USD.toFloat(), TOP_LIMIT_USD.toFloat(), currency)
			}
			Currency.EUR -> {
				binding.priceRangeSlider.valueFrom = BOTTOM_LIMIT_EUR.toFloat()
				binding.priceRangeSlider.valueTo = TOP_LIMIT_EUR.toFloat()
				binding.maxInputValue.filters = arrayOf<InputFilter>(
					MinMaxFilter(BOTTOM_LIMIT_EUR.toFloat(), TOP_LIMIT_EUR.toFloat())
				)
				processLimits(BOTTOM_LIMIT_EUR.toFloat(), TOP_LIMIT_EUR.toFloat(), currency)
			}
		}
	}

	private fun processLimits(bottomLimit: Float, topLimit: Float, currency: Currency) {
		this.bottomLimit = bottomLimit
		this.topLimit = topLimit

		binding.maxCurrency.text = currency.getCurrencySymbol(context)
		binding.minCurrency.text = currency.getCurrencySymbol(context)

		if (binding.minInputValue.text.toString() != bottomLimit.toInt().toString()) {
			binding.minInputValue.setText(bottomLimit.toInt().toString())
		}

		if (binding.maxInputValue.text.toString() != topLimit.toInt().toString()) {
			binding.maxInputValue.setText(topLimit.toInt().toString())
		}

		val currentSlider = binding.priceRangeSlider.values
		if (currentSlider[0] != bottomLimit || currentSlider[1] != topLimit) {
			binding.priceRangeSlider.setValues(bottomLimit, topLimit)
		}
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

	inner class MinMaxFilter(val minValue: Float, val maxValue: Float) : InputFilter {

		override fun filter(
			source: CharSequence, start: Int,
			end: Int, dest: Spanned,
			dStart: Int, dEnd: Int
		): CharSequence? {
			try {
				val input = (dest.toString() + source.toString()).toFloat()
				return if (isInRange(minValue, maxValue, input)) {
					null
				} else {
					maxValue.toString()
				}
			} catch (e: NumberFormatException) {
				Timber.w(e)
			}
			return "0"
		}

		private fun isInRange(a: Float, b: Float, c: Float): Boolean =
			if (b > a) c in a..b else c in b..a
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
