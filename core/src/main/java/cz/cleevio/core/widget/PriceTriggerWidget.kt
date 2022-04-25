package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetTriggerPriceBinding
import cz.cleevio.core.model.PriceTriggerValue
import lightbase.core.extensions.layoutInflater
import timber.log.Timber
import java.math.BigDecimal

class PriceTriggerWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetTriggerPriceBinding

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetTriggerPriceBinding.inflate(layoutInflater, this)
	}

	fun setupData(currentCryptoPrice: Float) {
		binding.currentCryptoPrice.text = resources.getString(
			R.string.widget_trigger_price_curr_crypto_price,
			currentCryptoPrice
		)
	}

	fun getTriggerType(): TriggerType = when (binding.priceTriggerType.checkedRadioButtonId) {
		R.id.price_below -> TriggerType.PRICE_IS_BELOW
		R.id.price_above -> TriggerType.PRICE_IS_ABOVE
		else -> TriggerType.NONE
	}

	fun getTriggerValue(): BigDecimal? {
		val currentValue = binding.priceEdit.text.toString()
		if (currentValue.isNotBlank()) {
			try {
				return BigDecimal(currentValue)
			} catch (e: NumberFormatException) {
				Timber.d("not a number $currentValue")
			}
		}
		return null
	}

	fun getPriceTriggerValue(): PriceTriggerValue = PriceTriggerValue(
		type = getTriggerType(),
		value = getTriggerValue()
	)
}

enum class TriggerType {
	NONE, PRICE_IS_BELOW, PRICE_IS_ABOVE
}