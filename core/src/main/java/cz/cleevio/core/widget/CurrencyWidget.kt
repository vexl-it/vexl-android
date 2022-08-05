package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetCurrencyBinding
import cz.cleevio.core.model.Currency
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater
import org.koin.core.component.KoinComponent

class CurrencyWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

	private lateinit var binding: WidgetCurrencyBinding

	var onCurrencyPicked: ((Currency) -> Unit)? = null

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetCurrencyBinding.inflate(layoutInflater, this)

		binding.currencyRadiogroup.setOnCheckedChangeListener { _, id ->
			when (id) {
				R.id.currency_czk -> onCurrencyPicked?.invoke(Currency.CZK)
				R.id.currency_eur -> onCurrencyPicked?.invoke(Currency.EUR)
				R.id.currency_usd -> onCurrencyPicked?.invoke(Currency.USD)
				else -> onCurrencyPicked?.invoke(Currency.CZK)
			}
		}
	}
}
