package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetCurrencyPriceChartBinding
import cz.cleevio.core.utils.formatAsPercentage
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import lightbase.core.extensions.layoutInflater
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.math.BigDecimal

class CurrencyPriceChartWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

	private lateinit var binding: WidgetCurrencyPriceChartBinding
	private lateinit var currentCryptoCurrencyPrice: CryptoCurrencies
	private var packed = true

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetCurrencyPriceChartBinding.inflate(layoutInflater, this)

		binding.wrapper.setOnClickListener {
			packed = !packed
			packView(packed)
		}

		binding.priceChartPeriodRadiogroup.setOnCheckedChangeListener { _, id ->
			updateChartData(id)
		}

		packView(packed)
	}

	fun setupData(currentCryptoCurrencyPrice: CryptoCurrencies) {
		this.currentCryptoCurrencyPrice = currentCryptoCurrencyPrice
		binding.currentPrice.text = currentCryptoCurrencyPrice.priceUsd.toString()

		updateChartData(binding.priceChartPeriodRadiogroup.checkedRadioButtonId)
	}

	private fun updateChartData(btnId: Int) {

		val priceChangePercentage = getValue(btnId)
		val priceChangeText = getText(btnId, priceChangePercentage)

		binding.cryptoChangePercentage.text = priceChangeText
		val drawable = if (priceChangePercentage < BigDecimal.ZERO) {
			ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_down, null)
		} else {
			ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_up, null)
		}
		binding.cryptoChangePercentage.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
	}

	private fun getText(btnId: Int, priceChangePercentage: BigDecimal): String {
		return when (btnId) {
			R.id.period_1_day -> resources.getString(R.string.marketplace_currency_variation_1_day, priceChangePercentage.formatAsPercentage())
			R.id.period_1_week -> resources.getString(R.string.marketplace_currency_variation_1_week, priceChangePercentage.formatAsPercentage())
			R.id.period_1_month -> resources.getString(R.string.marketplace_currency_variation_1_month, priceChangePercentage.formatAsPercentage())
			R.id.period_3_month -> resources.getString(R.string.marketplace_currency_variation_3_month, priceChangePercentage.formatAsPercentage())
			R.id.period_6_month -> resources.getString(R.string.marketplace_currency_variation_6_month, priceChangePercentage.formatAsPercentage())
			R.id.period_1_year -> resources.getString(R.string.marketplace_currency_variation_1_year, priceChangePercentage.formatAsPercentage())
			else -> {
				Timber.e("Unknown currency price period radio ID! '$id'")
				resources.getString(R.string.marketplace_currency_variation_1_day, priceChangePercentage.formatAsPercentage())
			}
		}
	}

	private fun getValue(btnId: Int): BigDecimal {
		return when (btnId) {
			R.id.period_1_day -> currentCryptoCurrencyPrice.priceChangePercentage24h
			R.id.period_1_week -> currentCryptoCurrencyPrice.priceChangePercentage7d
			R.id.period_1_month -> currentCryptoCurrencyPrice.priceChangePercentage30d
			R.id.period_3_month -> currentCryptoCurrencyPrice.priceChangePercentage60d
			R.id.period_6_month -> currentCryptoCurrencyPrice.priceChangePercentage200d
			R.id.period_1_year -> currentCryptoCurrencyPrice.priceChangePercentage1y
			else -> {
				Timber.e("Unknown currency price period radio ID! '$id'")
				currentCryptoCurrencyPrice.priceChangePercentage24h
			}
		}
	}

	private fun packView(packed: Boolean) {
		binding.packedGroup.isVisible = packed
		binding.unpackedGroup.isVisible = !packed
		val textColor =
			if (packed) {
				resources.getColor(R.color.yellow_darker, null)
			} else {
				resources.getColor(R.color.yellow_100, null)
			}
		binding.currentPrice.setTextColor(textColor)
		binding.currency.setTextColor(textColor)
	}
}