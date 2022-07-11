package cz.cleevio.core.utils.marketGraph

import android.content.Context
import com.github.mikephil.charting.formatter.ValueFormatter
import cz.cleevio.core.R

class MarketChartValueFormatter(private val context: Context) : ValueFormatter() {
	
	override fun getFormattedValue(value: Float): String {
		return context.getString(
			R.string.global_value_in_euro,
			value.toString()
		)
	}
}