package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetCurrencyPriceChartBinding
import cz.cleevio.core.model.CryptoCurrency
import cz.cleevio.core.model.Currency
import cz.cleevio.core.model.Currency.Companion.mapStringToCurrency
import cz.cleevio.core.model.MarketChartEntry
import cz.cleevio.core.utils.*
import cz.cleevio.core.utils.marketGraph.MarketChartUtils
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class CurrencyPriceChartWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

	private val graphUtils: MarketChartUtils by inject()
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository by inject()

	private lateinit var binding: WidgetCurrencyPriceChartBinding
	private var currentCryptoCurrencyPrice: CryptoCurrencies? = null
	private var marketChartData: MarketChartEntry? = null
	private var currency: Currency = encryptedPreferenceRepository.selectedCurrency.mapStringToCurrency()
	private var cryptoCurrency: CryptoCurrency = CryptoCurrency.BITCOIN
	private var packed = true
	private var dateTimeRange: DateTimeRange? = DateTimeRange.DAY

	var onPriceChartPeriodClicked: ((DateTimeRange) -> Unit)? = null

	init {
		setupUI()
	}

	fun setupCryptoCurrencies(currentCryptoCurrencyPrice: CryptoCurrencies?) {
		this.currentCryptoCurrencyPrice = currentCryptoCurrencyPrice
		binding.currentPrice.text =
			when (currency) {
				Currency.CZK -> currentCryptoCurrencyPrice?.priceCzk?.formatAsPrice()
				Currency.EUR -> currentCryptoCurrencyPrice?.priceEur?.formatAsPrice()
				else -> currentCryptoCurrencyPrice?.priceUsd?.formatAsPrice()
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

	fun updateCurrency(currency: Currency) {
		this.currency = currency
		updateCurrencyView()
		if (currentCryptoCurrencyPrice != null) {
			setupCryptoCurrencies(currentCryptoCurrencyPrice)
		}
	}

	@Suppress("MagicNumber")
	fun setupTimeRange(dateTimeRange: DateTimeRange?) {
		val numberOfDatesOnTimeline = if (dateTimeRange == DateTimeRange.WEEK) 7 else 5
		val dates = getDateIntervals(numberOfDatesOnTimeline, getChartTimeRange(dateTimeRange))

		this.dateTimeRange = dateTimeRange
		updateModifyingCellsVisibility()

		val formattedDates = if (dateTimeRange == DateTimeRange.DAY) {
			dates.map {
				val format = SimpleDateFormat("hh:mm")
				format.format(it)
			}
		} else {
			dates.map {
				val format = SimpleDateFormat("dd. LLL")
				format.format(it)
			}
		}

		if (formattedDates.size == 7) {
			binding.firstDate.text = formattedDates[0]
			binding.secondDate.text = formattedDates[1]
			binding.thirdDate.text = formattedDates[2]
			binding.forthDate.text = formattedDates[3]
			binding.fifthDate.text = formattedDates[4]
			binding.sixthDate.text = formattedDates[5]
			binding.seventhDate.text = formattedDates[6]
		} else if (formattedDates.size == 5) {
			binding.firstDate.text = formattedDates[0]
			binding.forthDate.text = formattedDates[1]
			binding.fifthDate.text = formattedDates[2]
			binding.sixthDate.text = formattedDates[3]
			binding.seventhDate.text = formattedDates[4]
		}

		showLoading(false)
	}

	private fun getDateIntervals(numberOfDates: Int, timeRange: Pair<String, String>): List<Date> {
		val rangeInLong = Pair(timeRange.first.toLong(), timeRange.second.toLong())
		val step = (rangeInLong.second - rangeInLong.first) / numberOfDates

		// first == from, second == to
		return (0 until numberOfDates).map {
			rangeInLong.first + it * step
		}.map {
			Date(it * MILLIS_FORMATTER)
		}
	}

	private fun updateChartData() {
		marketChartData?.let { data ->
			graphUtils.handleChartData(
				data,
				binding.largeChart
			)
			graphUtils.handleChartData(
				data,
				binding.smallChart,
				graphLineColor = R.color.yellow_darker
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
			showLoading(true)
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
		graphUtils.init(binding.smallChart, false, 0)
	}

	private fun updatePercentageData(btnId: Int = binding.priceChartPeriodRadiogroup.checkedRadioButtonId) {
		val priceChangePercentage = getValue(btnId)
		val priceChangeText = getText(btnId, priceChangePercentage)

		binding.cryptoChangePercentage.startAnimation(
			animateTextChange(context, binding.cryptoChangePercentage, R.anim.show_text_animation, priceChangeText)
		)

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
			R.id.period_1_day -> currentCryptoCurrencyPrice?.priceChangePercentage24h ?: BigDecimal.ZERO
			R.id.period_1_week -> currentCryptoCurrencyPrice?.priceChangePercentage7d ?: BigDecimal.ZERO
			R.id.period_1_month -> currentCryptoCurrencyPrice?.priceChangePercentage30d ?: BigDecimal.ZERO
			R.id.period_3_month -> currentCryptoCurrencyPrice?.priceChangePercentage60d ?: BigDecimal.ZERO
			R.id.period_6_month -> currentCryptoCurrencyPrice?.priceChangePercentage200d ?: BigDecimal.ZERO
			R.id.period_1_year -> currentCryptoCurrencyPrice?.priceChangePercentage1y ?: BigDecimal.ZERO
			else -> {
				Timber.e("Unknown currency price period radio ID! '$id'")
				currentCryptoCurrencyPrice?.priceChangePercentage24h ?: BigDecimal.ZERO
			}
		}
	}

	private fun packView(packed: Boolean) {
		binding.cryptoChangePercentage.clearAnimation()

		binding.packedGroup.isVisible = packed
		binding.unpackedGroup.isVisible = !packed
		if (packed) {
			binding.chartProgress.isVisible = false
		}
		updateModifyingCellsVisibility()
		val textColor =
			if (packed) {
				resources.getColor(R.color.yellow_darker, null)
			} else {
				resources.getColor(R.color.yellow_100, null)
			}
		binding.currentPrice.setTextColor(textColor)
		binding.suffixCurrency.setTextColor(textColor)
		binding.prefixCurrency.setTextColor(textColor)

		binding.currencyName.text =
			if (cryptoCurrency == CryptoCurrency.BITCOIN) {
				resources.getString(R.string.marketplace_currency_bitcoin)
			} else {
				""
			}
	}

	private fun updateModifyingCellsVisibility() {
		binding.modifyingCells.isVisible = !packed && dateTimeRange == DateTimeRange.WEEK
	}

	private fun showLoading(isVisible: Boolean) {
		if (!packed && !isVisible) {
			binding.largeChart.isVisible = true
			binding.chartProgress.isVisible = false
		} else if (!packed && isVisible) {
			binding.largeChart.isInvisible = true
			binding.chartProgress.isVisible = true
		} else {
			binding.largeChart.isVisible = false
			binding.chartProgress.isVisible = false
		}
	}

	private fun updateCurrencyView() {
		when (currency) {
			Currency.CZK -> {
				binding.prefixCurrency.isVisible = false
				binding.suffixCurrency.isVisible = true
				binding.suffixCurrency.text = resources.getString(R.string.general_czk_sign)
			}
			Currency.EUR -> {
				binding.prefixCurrency.isVisible = true
				binding.suffixCurrency.isVisible = false
				binding.prefixCurrency.text = resources.getString(R.string.general_eur_sign)
			}
			Currency.USD -> {
				binding.prefixCurrency.isVisible = true
				binding.suffixCurrency.isVisible = false
				binding.prefixCurrency.text = resources.getString(R.string.general_usd_sign)
			}
		}
	}

	companion object {
		private const val MILLIS_FORMATTER = 1000L
	}
}