package cz.cleevio.core.utils.marketGraph

import android.graphics.Canvas
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class MarketLineChartRender constructor(
	chart: LineDataProvider,
	animator: ChartAnimator,
	viewPortHandler: ViewPortHandler,
	private val minEntry: Entry?,
	private val maxEntry: Entry?,
	private val verticalOffset: Float,
	private val horizontalOffset: Float
) : LineChartRenderer(chart, animator, viewPortHandler) {

	@Suppress("ComplexMethod", "NestedBlockDepth", "LoopWithTooManyJumpStatements", "MagicNumber")
	override fun drawValues(c: Canvas?) {
		val dataSets = mChart.lineData.dataSets
		for (i in dataSets.indices) {
			val dataSet = dataSets[i]
			if (!shouldDrawValues(dataSet) || dataSet.entryCount < 1) continue

			// apply the text-styling defined by the DataSet
			applyValueTextStyle(dataSet)
			val trans = mChart.getTransformer(dataSet.axisDependency)

			mXBounds[mChart] = dataSet
			val positions = trans.generateTransformedValuesLine(
				dataSet, mAnimator.phaseX, mAnimator.phaseY, mXBounds.min, mXBounds.max
			)
			val formatter = dataSet.valueFormatter
			var j = 0

			val thresholdOffset = Utils.convertDpToPixel(4f)
			val leftThreshold = mViewPortHandler.offsetLeft() + thresholdOffset
			val rightThreshold = mViewPortHandler.chartWidth - mViewPortHandler.offsetRight() - thresholdOffset

			while (j < positions.size) {
				val x = positions[j]
				val y = positions[j + 1]
				if (!mViewPortHandler.isInBoundsRight(x)) break
				if (!mViewPortHandler.isInBoundsLeft(x) || !mViewPortHandler.isInBoundsY(y)) {
					j += 2
					continue
				}

				val entry = dataSet.getEntryForIndex(j / 2 + mXBounds.min)
				if (minEntry == entry) {
					val xLeftOffset = if (x <= leftThreshold) horizontalOffset else 0f
					val xRightOffset = if (x >= rightThreshold) -horizontalOffset else 0f
					drawValue(
						c,
						formatter.getPointLabel(entry),
						x + xLeftOffset + xRightOffset,
						y + verticalOffset,
						dataSet.getValueTextColor(j / 2)
					)
				} else if (maxEntry == entry) {
					val xLeftOffset = if (x <= leftThreshold) horizontalOffset else 0f
					val xRightOffset = if (x >= rightThreshold) -horizontalOffset else 0f
					drawValue(
						c,
						formatter.getPointLabel(entry),
						x + xLeftOffset + xRightOffset,
						y - verticalOffset,
						dataSet.getValueTextColor(j / 2)
					)
				}
				j += 2
			}
		}
	}
}