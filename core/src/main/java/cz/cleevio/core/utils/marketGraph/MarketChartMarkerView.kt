package cz.cleevio.core.utils.marketGraph

import android.annotation.SuppressLint
import android.content.Context
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.utils.MPPointF
import cz.cleevio.core.R

@SuppressLint("ViewConstructor")
class MarketChartMarkerView constructor(context: Context, isDecreasing: Boolean)
	: MarkerView(context, if (isDecreasing) R.layout.view_market_chart_marker_red else R.layout.view_market_chart_marker_green) {
	private var pointOffset: MPPointF? = null
	
	override fun getOffset(): MPPointF? {
		if (pointOffset == null) {
			pointOffset = MPPointF(
				(width / 2F).unaryMinus(),
				(height / 2F).unaryMinus()
			)
		}
		return pointOffset
	}
}