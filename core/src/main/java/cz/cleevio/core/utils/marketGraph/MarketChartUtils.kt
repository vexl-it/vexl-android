package cz.cleevio.core.utils.marketGraph

import android.content.Context
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import cz.cleevio.core.R
import cz.cleevio.core.model.MarketChartEntry
import kotlinx.coroutines.flow.MutableStateFlow
import lightbase.core.extensions.dpValueToPx

class MarketChartUtils constructor(
	private val context: Context
) {
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

	// Call this method in init view of fragment
	fun init(chartView: LineChart, isTouchEnabled: Boolean, viewOffset: Int? = null) {
		initTradingChart(chartView, isTouchEnabled, viewOffset)
	}

	fun handleChartData(
		marketChartData: MarketChartEntry,
		chartView: LineChart,
		isDrawValuesEnabled: Boolean = false,
		viewOffset: Int? = null,
		graphLineColor: Int? = null
	) {
		chartView.highlightValues(emptyArray())

		val lineDataSet = LineDataSet(marketChartData.entries, "Label").apply {
			lineWidth = LINE_WIDTH
			valueTextSize = VALUE_TEXT_SIZE
			cubicIntensity = CUBIC_INTENSITY
			mode = LineDataSet.Mode.CUBIC_BEZIER
			highLightColor = ContextCompat.getColor(context, R.color.yellow)
			color = ContextCompat.getColor(context, graphLineColor ?: R.color.yellow)
			fillDrawable = ContextCompat.getDrawable(context, R.drawable.background_gradient_graph_yellow)
			valueTextColor = ContextCompat.getColor(context, R.color.yellow)
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
				minEntry = marketChartData.minEntry,
				maxEntry = marketChartData.maxEntry,
				verticalOffset = offset,
				horizontalOffset = offset
			)
			data = LineData(lineDataSet)
			invalidate()
		}
	}

	private fun initTradingChart(chartView: LineChart, isTouchEnabled: Boolean, viewOffset: Int? = null) {
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
			setPinchZoom(false)
			setTouchEnabled(isTouchEnabled)
			setScaleEnabled(false)
			setDrawGridBackground(false)
			setMaxVisibleValueCount(MAX_VISIBLE_VALUE)
			setOnChartValueSelectedListener(chartValueSelectedListener)
			setNoDataText(context.getString(R.string.trading_no_data))
			setNoDataTextColor(ContextCompat.getColor(context, R.color.black))
			invalidate()
		}
	}

	companion object {
		private const val MAX_VISIBLE_VALUE = 500
		private const val VIEW_PORT_OFFSET = 16
		private const val VERTICAL_VIEW_PORT_OFFSET = 20
		private const val LINE_WIDTH = 2f
		private const val VALUE_TEXT_SIZE = 2f
		private const val CUBIC_INTENSITY = 0.1f
	}
}