package cz.cleevio.core.utils.marketGraph

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.lyft.kronos.KronosClock
import cz.cleevio.core.R
import cz.cleevio.core.model.MarketChartEntry
import cz.cleevio.core.utils.DateTimeRange
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.network.request.market.MarketChartRequest
import cz.cleevio.repository.repository.marketplace.CryptoCurrencyRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import lightbase.core.extensions.dpValueToPx

class MarketChartUtils constructor(
	private val context: Context,
	private val cryptoCurrencyRepository: CryptoCurrencyRepository,
	private val kronosClock: KronosClock
) {
	val marketRatesStateFlow: MutableStateFlow<Resource<MarketChartEntry>?> = MutableStateFlow(null)
	
	val dateTimeRangeLiveData = MutableLiveData<DateTimeRange>().apply { postValue(DateTimeRange.DAY) }
	val currentTradingDiffPercentage = MutableLiveData<Double>().apply { postValue(0.0) }
	val currentTradingValue = MutableLiveData<Double>().apply { postValue(0.0) }
	val dataUpdated = MutableLiveData<Unit>()
	val chartValueSelectedInvoked = MutableLiveData<Pair<Entry?, Highlight?>>()
	val chartNothingSelectedInvoked = MutableLiveData<Unit>()

	private val chartValueSelectedListener: OnChartValueSelectedListener = object : OnChartValueSelectedListener {
		override fun onValueSelected(e: Entry?, h: Highlight?) {
			chartValueSelectedInvoked.postValue(Pair(e, h))
		}
		
		override fun onNothingSelected() {
			chartNothingSelectedInvoked.postValue(Unit)
		}
	}

	// Call this method in init view of fragment
	@DelicateCoroutinesApi
	fun init(chartView: LineChart, tradingValueView: TextView, isTouchEnabled: Boolean, viewOffset: Int? = null) {
		initTradingChart(chartView, tradingValueView, isTouchEnabled, viewOffset)
		getMarketRates(DateTimeRange.MONTH)
	}
	
	// Call in onResume of fragment
	@DelicateCoroutinesApi
	fun syncMarketData() {
		GlobalScope.launch(Dispatchers.IO) {
			cryptoCurrencyRepository.getMarketChartData(MarketChartRequest("0", "0", "bitcoin"))
		}
	}
	
	fun handleChartData(marketChartEntry: MarketChartEntry, chartView: LineChart, tradingValueView: TextView? = null, isDrawValuesEnabled: Boolean, viewOffset: Int? = null) {
		chartView.highlightValues(emptyArray())
		val lineDataSet = LineDataSet(marketChartEntry.entries, "Label").apply {
			lineWidth = 2f
			valueTextSize = 12f
			cubicIntensity = 0.1f
			mode = LineDataSet.Mode.CUBIC_BEZIER
			highLightColor = ContextCompat.getColor(context, R.color.yellow)
			
			when {
				((currentTradingDiffPercentage.value ?: 0.0) == INV_ZERO_DOUBLE) ||
					(currentTradingDiffPercentage.value ?: 0.0) == ZERO_DOUBLE -> {
					color = ContextCompat.getColor(context, R.color.yellow)
					fillDrawable = ContextCompat.getDrawable(context, R.drawable.background_gradient_graph_yellow)
					valueTextColor = ContextCompat.getColor(context, R.color.yellow)
				}
				(currentTradingDiffPercentage.value ?: 0.0) < ZERO_DOUBLE -> {
					color = ContextCompat.getColor(context, R.color.yellow)
					fillDrawable = ContextCompat.getDrawable(context, R.drawable.background_gradient_graph_yellow)
					valueTextColor = ContextCompat.getColor(context, R.color.yellow)
				}
				(currentTradingDiffPercentage.value ?: 0.0) > ZERO_DOUBLE -> {
					color = ContextCompat.getColor(context, R.color.yellow)
					fillDrawable = ContextCompat.getDrawable(context, R.drawable.background_gradient_graph_yellow)
					valueTextColor = ContextCompat.getColor(context, R.color.yellow)
				}
			}
			
			//valueTypeface = ResourcesCompat.getFont(context, R.font.barlow_medium)
			valueFormatter = MarketChartValueFormatter(context)
			setDrawFilled(true)
			setDrawCircles(false)
			setDrawValues(isDrawValuesEnabled)
			setDrawIcons(false)
			setDrawHorizontalHighlightIndicator(false)
		}
		
		with(chartView) {
			val offset = context.dpValueToPx(viewOffset ?: VIEW_PORT_OFFSET)
			renderer = MarketLineChartRender(
				chart = this,
				animator = animator,
				viewPortHandler = viewPortHandler,
				minEntry = marketChartEntry.minEntry,
				maxEntry = marketChartEntry.maxEntry,
				verticalOffset = offset,
				horizontalOffset = offset
			)
			data = LineData(lineDataSet)
			marker = MarketChartMarkerView(context, (currentTradingDiffPercentage.value ?: 0.0) < ZERO_DOUBLE)
			invalidate()
		}
		tradingValueView?.text = context.getString(
			R.string.global_value_in_euro,
			currentTradingValue.value ?: "0"
		)
		dataUpdated.postValue(Unit)
	}
	
	fun handleTradingDiffPercentageChange(percentagePriceView: TextView, percentage: Double) {
		when {
			((percentage == ZERO_DOUBLE) || (percentage == INV_ZERO_DOUBLE)) -> {
				percentagePriceView.text = context.getString(R.string.global_value_in_percentage, "0")
				setTradingDiffPercentageView(R.color.yellow, null, percentagePriceView)
			}
			percentage < ZERO_DOUBLE -> {
				percentagePriceView.text = context.getString(R.string.global_value_in_percentage, percentage.toString())
				setTradingDiffPercentageView(R.color.yellow, R.drawable.ic_arrow_down, percentagePriceView)
			}
			percentage > ZERO_DOUBLE -> {
				percentagePriceView.text = context.getString(R.string.global_plus_value_in_percentage, percentage.toString())
				setTradingDiffPercentageView(R.color.yellow, R.drawable.ic_arrow_up, percentagePriceView)
			}
		}
	}

	// todo udpate
	@DelicateCoroutinesApi
	fun getMarketRates(dateTimeRange: DateTimeRange) {
		GlobalScope.launch(Dispatchers.IO) {
			marketRatesStateFlow.value = Resource.loading()
			val response = cryptoCurrencyRepository.getMarketChartData(
				MarketChartRequest(
					from = "",
					to = "",
					currency = "bitcoin"
				)
			)
			dateTimeRangeLiveData.postValue(dateTimeRange)
			
			if (response.status is Status.Error || response.data == null || response.data?.entries == null) {
				marketRatesStateFlow.value = Resource.error(response.errorIdentification)
			} else {
				response.data?.let { marketRates ->
					currentTradingDiffPercentage.postValue(marketRates.entries.firstOrNull()?.toDouble() ?: 0.0)
					
					var x = -1f
					val entries = marketRates.entries.map { marketRate ->
						x++
						Entry(
							x,
							marketRate.toFloat()
						)
					}.toMutableList()
					
					// If there is only one entry than double it for correct view
					if (entries.size == 1) {
						entries.firstOrNull { entry ->
							val newEntry = entry.copy()
							newEntry.x++
							entries.add(newEntry)
						}
					}
					
					val minEntry = entries.minByOrNull { it.y }
					val maxEntry = entries.maxByOrNull { it.y }
					
					marketRatesStateFlow.value =
						Resource.success(
							MarketChartEntry(
								minEntry = minEntry,
								maxEntry = maxEntry,
								entries = entries
							)
						)
				}
			}
		}
	}
	
	fun showUpdatedPrice(price: Double?, tradingValueView: TextView?) {
		tradingValueView?.text = context.getString(
			R.string.global_value_in_euro,
			price ?: "0"
		)
	}
	
	private fun initTradingChart(chartView: LineChart, tradingValueView: TextView? = null, isTouchEnabled: Boolean, viewOffset: Int? = null) {
		val viewPortOffset = context.dpValueToPx(viewOffset ?: VIEW_PORT_OFFSET)
		val verticalViewPortOffset = context.dpValueToPx(viewOffset ?: VERTICAL_VIEW_PORT_OFFSET)
		with(chartView) {
			setViewPortOffsets(viewPortOffset, verticalViewPortOffset, viewPortOffset, verticalViewPortOffset)
			isDragEnabled = true
			legend.isEnabled = false
			xAxis.isEnabled = false
			axisLeft.isEnabled = false
			axisRight.isEnabled = false
			description.isEnabled = false
			isAutoScaleMinMaxEnabled = true
			isHighlightPerTapEnabled = true
			isHighlightPerDragEnabled = true
			marker = MarketChartMarkerView(context, false)
			setPinchZoom(false)
			setTouchEnabled(isTouchEnabled)
			setScaleEnabled(false)
			setDrawGridBackground(false)
			setMaxVisibleValueCount(MAX_VISIBLE_VALUE)
			setOnChartValueSelectedListener(chartValueSelectedListener)
			setNoDataText(context.getString(R.string.trading_no_data))
			setNoDataTextColor(ContextCompat.getColor(context, R.color.black))
			//setNoDataTextTypeface(ResourcesCompat.getFont(context, R.font.barlow_medium))
			invalidate()
		}
		tradingValueView?.text = context.getString(
			R.string.global_value_in_euro,
			currentTradingValue.value?.toString() ?: "0"
		)
	}
	
	private fun setTradingDiffPercentageView(colorId: Int, drawableStartId: Int?, percentagePriceView: TextView) {
		percentagePriceView.run {
			setTextColor(ContextCompat.getColor(context, colorId))
			setCompoundDrawablesWithIntrinsicBounds(
				drawableStartId?.let { ContextCompat.getDrawable(context, it) },
				null,
				null,
				null
			)
		}
	}
	
	companion object {
		private const val ZERO_DOUBLE = 0.0
		private const val INV_ZERO_DOUBLE = -0.0
		private const val MAX_VISIBLE_VALUE = 500
		private const val VIEW_PORT_OFFSET = 16
		private const val VERTICAL_VIEW_PORT_OFFSET = 20
	}
}