package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.RadioGroup
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

	private var onCheckedChangeListener: RadioGroup.OnCheckedChangeListener? = null

	init {
		setupUI()
	}

	fun selectCurrencyManually(currency: Currency) {
		binding.currencyRadiogroup.setOnCheckedChangeListener(null)
		when (currency) {
			Currency.CZK -> binding.currencyRadiogroup.check(R.id.currency_czk)
			Currency.EUR -> binding.currencyRadiogroup.check(R.id.currency_eur)
			Currency.USD -> binding.currencyRadiogroup.check(R.id.currency_usd)
		}
		binding.currencyRadiogroup.setOnCheckedChangeListener(onCheckedChangeListener)
	}

	private fun setupUI() {
		binding = WidgetCurrencyBinding.inflate(layoutInflater, this)

		onCheckedChangeListener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
			when (checkedId) {
				R.id.currency_czk -> onCurrencyPicked?.invoke(Currency.CZK)
				R.id.currency_eur -> onCurrencyPicked?.invoke(Currency.EUR)
				R.id.currency_usd -> onCurrencyPicked?.invoke(Currency.USD)
				else -> onCurrencyPicked?.invoke(Currency.CZK)
			}
		}

		binding.currencyRadiogroup.setOnCheckedChangeListener(onCheckedChangeListener)
	}
}
