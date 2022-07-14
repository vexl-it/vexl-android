package cz.cleevio.core.utils.marketGraph

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import cz.cleevio.core.R
import cz.cleevio.core.model.MarketChartEntry
import cz.cleevio.repository.repository.marketplace.CryptoCurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import lightbase.core.extensions.dpValueToPx

class MarketChartUtils constructor(
	private val context: Context,
	private val cryptoCurrencyRepository: CryptoCurrencyRepository
) {
	val currentTradingValue: MutableStateFlow<Double> = MutableStateFlow(0.0)
	val dataUpdated: MutableStateFlow<Unit> = MutableStateFlow(Unit)
	val chartValueSelectedInvoked: MutableStateFlow<Pair<Entry?, Highlight?>?> = MutableStateFlow(null)
	val chartNothingSelectedInvoked: MutableStateFlow<Unit> = MutableStateFlow(Unit)

	private val chartValueSelectedListener: OnChartValueSelectedListener = object : OnChartValueSelectedListener {
		override fun onValueSelected(e: Entry?, h: Highlight?) {
			chartValueSelectedInvoked.tryEmit(Pair(e, h))
		}

		override fun onNothingSelected() {
			chartNothingSelectedInvoked.tryEmit(Unit)
		}
	}

	private var marketData: MarketChartEntry? = null

	fun setMarketData(marketData: MarketChartEntry?) {
		this.marketData = marketData
	}

	// Call this method in init view of fragment
	fun init(chartView: LineChart, tradingValueView: TextView, isTouchEnabled: Boolean, viewOffset: Int? = null) {
		initTradingChart(chartView, tradingValueView, isTouchEnabled, viewOffset)
	}

	fun handleChartData(lineDataSet: LineDataSet, marketChartEntry: MarketChartEntry, chartView: LineChart, tradingValueView: TextView? = null, isDrawValuesEnabled: Boolean, viewOffset: Int? = null) {
		chartView.highlightValues(emptyArray())

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
			// todo update
			marker = MarketChartMarkerView(context, false)
			invalidate()
		}
		tradingValueView?.text = context.getString(
			R.string.global_value_in_euro,
			currentTradingValue.value
		)
		dataUpdated.tryEmit(Unit)
	}

	// todo udpate
	fun setMarketRates() {

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
			currentTradingValue.value.toString()
		)
	}


	companion object {
		private const val ZERO_DOUBLE = 0.0
		private const val INV_ZERO_DOUBLE = -0.0
		private const val MAX_VISIBLE_VALUE = 500
		private const val VIEW_PORT_OFFSET = 16
		private const val VERTICAL_VIEW_PORT_OFFSET = 20
	}
}