package cz.cleeevio.vexl.marketplace.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.WidgetCurrencyPriceChartBinding
import cz.cleevio.core.utils.showOrGone
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import lightbase.core.extensions.layoutInflater
import org.koin.core.component.KoinComponent
import timber.log.Timber


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
			binding.cryptoChangePercentage.text = when (id) {
				R.id.period_1_day -> currentCryptoCurrencyPrice.priceChangePercentage24h.toString()
				R.id.period_1_week -> currentCryptoCurrencyPrice.priceChangePercentage7d.toString()
				R.id.period_1_month -> currentCryptoCurrencyPrice.priceChangePercentage30d.toString()
				R.id.period_3_month -> currentCryptoCurrencyPrice.priceChangePercentage60d.toString()
				R.id.period_6_month -> currentCryptoCurrencyPrice.priceChangePercentage200d.toString()
				R.id.period_1_year -> currentCryptoCurrencyPrice.priceChangePercentage1y.toString()
				else -> {
					Timber.e("Unknown currency price period radio ID! '$id'")
					currentCryptoCurrencyPrice.priceChangePercentage24h.toString()
				}
			}
		}
	}

	fun setupData(currentCryptoCurrencyPrice: CryptoCurrencies) {
		this.currentCryptoCurrencyPrice = currentCryptoCurrencyPrice
		binding.currentPrice.text = currentCryptoCurrencyPrice.priceUsd.toString()
	}

	private fun packView(packed: Boolean) {
		binding.packedGroup.showOrGone(packed)
		binding.unpackedGroup.showOrGone(!packed)
	}

}