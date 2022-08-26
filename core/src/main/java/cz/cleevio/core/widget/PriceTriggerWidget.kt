package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetTriggerPriceBinding
import cz.cleevio.core.model.Currency
import cz.cleevio.core.model.Currency.Companion.getCurrencySymbol
import cz.cleevio.core.model.Currency.Companion.mapStringToCurrency
import cz.cleevio.core.model.PriceTriggerValue
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.math.BigDecimal

class PriceTriggerWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

	private lateinit var currency: Currency
	private lateinit var binding: WidgetTriggerPriceBinding
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository by inject()


	var onFocusChangeListener: ((Boolean) -> Unit)? = null

	init {
		setupUI()
		setCurrency(encryptedPreferenceRepository.selectedCurrency.mapStringToCurrency())
	}

	private fun setupUI() {
		binding = WidgetTriggerPriceBinding.inflate(layoutInflater, this)

		binding.priceEdit.setText(BigDecimal.ZERO.toString())
		binding.priceEdit.setOnFocusChangeListener { _, hasFocus ->
			onFocusChangeListener?.invoke(hasFocus)
		}

		binding.clearFilterBtn.setOnClickListener {
			binding.priceEdit.setText(BigDecimal.ZERO.toString())
			setCurrency(encryptedPreferenceRepository.selectedCurrency.mapStringToCurrency())
		}
	}

	fun setupData(currentCryptoPrice: Float) {
		binding.currentCryptoPrice.text = resources.getString(
			R.string.widget_trigger_price_curr_crypto_price,
			currentCryptoPrice
		)
	}

	fun setCurrency(currentCurrency: Currency) {
		this.currency = currentCurrency
		binding.currency.text = currentCurrency.getCurrencySymbol(context)
	}

	private fun getTriggerType(): TriggerType = when (binding.priceTriggerType.checkedRadioButtonId) {
		R.id.price_below -> TriggerType.PRICE_IS_BELOW
		R.id.price_above -> TriggerType.PRICE_IS_ABOVE
		else -> TriggerType.NONE
	}

	private fun getTriggerValue(): BigDecimal {
		val currentValue = binding.priceEdit.text.toString()
		if (currentValue.isNotBlank()) {
			try {
				return BigDecimal(currentValue)
			} catch (e: NumberFormatException) {
				Timber.d("not a number $currentValue")
			}
		}
		return BigDecimal.ZERO
	}

	private fun getTriggerCurrency(): String {
		return this.currency.name
	}

	fun getPriceTriggerValue(): PriceTriggerValue = PriceTriggerValue(
		type = getTriggerType(),
		value = getTriggerValue(),
		currency = getTriggerCurrency()
	)

	fun setPriceTriggerValue(data: PriceTriggerValue) {
		when (data.type) {
			TriggerType.NONE -> {
				binding.priceBelow.isChecked = false
				binding.priceAbove.isChecked = false
			}
			TriggerType.PRICE_IS_BELOW -> {
				binding.priceBelow.isChecked = true
				binding.priceAbove.isChecked = false
			}
			TriggerType.PRICE_IS_ABOVE -> {
				binding.priceBelow.isChecked = false
				binding.priceAbove.isChecked = true
			}
		}
		binding.priceEdit.setText(data.value.toString())
		setCurrency(data.currency.mapStringToCurrency())
	}
}

enum class TriggerType {
	NONE, PRICE_IS_BELOW, PRICE_IS_ABOVE
}