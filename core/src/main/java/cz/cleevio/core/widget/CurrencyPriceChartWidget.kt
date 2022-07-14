package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.github.mikephil.charting.data.LineDataSet
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetCurrencyPriceChartBinding
import cz.cleevio.core.model.CryptoCurrency
import cz.cleevio.core.model.Currency
import cz.cleevio.core.model.MarketChartEntry
import cz.cleevio.core.utils.DateTimeRange
import cz.cleevio.core.utils.formatAsPercentage
import cz.cleevio.core.utils.marketGraph.MarketChartUtils
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import lightbase.core.extensions.layoutInflater
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.math.BigDecimal

class CurrencyPriceChartWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

	private lateinit var binding: WidgetCurrencyPriceChartBinding
	private lateinit var currentCryptoCurrencyPrice: CryptoCurrencies
	private var marketChartData: MarketChartEntry? = null
	private var currency: Currency = Currency.USD
	private var cryptoCurrency: CryptoCurrency = CryptoCurrency.BITCOIN
	private var packed = true

	var onPriceChartPeriodClicked: ((DateTimeRange) -> Unit)? = null

	private val graphUtils: MarketChartUtils by inject()

	init {
		setupUI()
	}

	fun setupCryptoCurrencies(currentCryptoCurrencyPrice: CryptoCurrencies) {
		this.currentCryptoCurrencyPrice = currentCryptoCurrencyPrice
		binding.currentPrice.text =
			when (currency) {
				Currency.CZK -> currentCryptoCurrencyPrice.priceCzk.toString()
				Currency.EUR -> currentCryptoCurrencyPrice.priceEur.toString()
				else -> currentCryptoCurrencyPrice.priceUsd.toString()
			}

		updatePercentageData()
	}

	fun setupCurrencies(currency: Currency, cryptoCurrency: CryptoCurrency) {
		this.currency = currency
		this.cryptoCurrency = cryptoCurrency
	}

	fun setupMarketData(marketChartEntry: MarketChartEntry) {
		this.marketChartData = marketChartEntry
		updateChartData()
	}

	private fun updateChartData() {
		marketChartData?.let { data ->
			graphUtils.handleChartData(
				data,
				binding.largeChart
			)
		}
	}

	private fun setupUI() {
		binding = WidgetCurrencyPriceChartBinding.inflate(layoutInflater, this)

		binding.wrapper.setOnClickListener {
			packed = !packed
			packView(packed)
		}

		binding.priceChartPeriodRadiogroup.setOnCheckedChangeListener { _, id ->
			updatePercentageData(id)
			onPriceChartPeriodClicked?.invoke(
				when (id) {
					R.id.period_1_day -> DateTimeRange.DAY
					R.id.period_1_week -> DateTimeRange.WEEK
					R.id.period_1_month -> DateTimeRange.MONTH
					R.id.period_3_month -> DateTimeRange.THREE_MONTHS
					R.id.period_6_month -> DateTimeRange.SIX_MONTHS
					R.id.period_1_year -> DateTimeRange.YEAR
					else -> {
						DateTimeRange.DAY
					}
				}
			)
		}

		packView(packed)
		graphUtils.init(binding.largeChart, false, 0)
	}

	private fun updatePercentageData(btnId: Int = binding.priceChartPeriodRadiogroup.checkedRadioButtonId) {
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
			R.id.period_1_day -> resources.getString(
				R.string.marketplace_currency_variation_1_day,
				priceChangePercentage.formatAsPercentage()
			)
			R.id.period_1_week -> resources.getString(
				R.string.marketplace_currency_variation_1_week,
				priceChangePercentage.formatAsPercentage()
			)
			R.id.period_1_month -> resources.getString(
				R.string.marketplace_currency_variation_1_month,
				priceChangePercentage.formatAsPercentage()
			)
			R.id.period_3_month -> resources.getString(
				R.string.marketplace_currency_variation_3_month,
				priceChangePercentage.formatAsPercentage()
			)
			R.id.period_6_month -> resources.getString(
				R.string.marketplace_currency_variation_6_month,
				priceChangePercentage.formatAsPercentage()
			)
			R.id.period_1_year -> resources.getString(
				R.string.marketplace_currency_variation_1_year,
				priceChangePercentage.formatAsPercentage()
			)
			else -> {
				Timber.e("Unknown currency price period radio ID! '$id'")
				resources.getString(
					R.string.marketplace_currency_variation_1_day,
					priceChangePercentage.formatAsPercentage()
				)
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

		if (currency == Currency.CZK) {
			binding.prefixCurrency.isVisible = false
			binding.suffixCurrency.isVisible = true

			binding.suffixCurrency.setTextColor(textColor)
		} else {
			binding.prefixCurrency.isVisible = true
			binding.suffixCurrency.isVisible = false

			binding.prefixCurrency.setTextColor(textColor)
			binding.prefixCurrency.text = if (currency == Currency.EUR) resources.getString(R.string.general_eur_sign) else resources.getString(R.string.general_usd_sign)
		}

		binding.currencyName.text = if (cryptoCurrency == CryptoCurrency.BITCOIN) resources.getString(R.string.marketplace_currency_bitcoin) else ""
	}
}