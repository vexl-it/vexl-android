package cz.cleevio.core.model

import com.github.mikephil.charting.data.Entry

data class MarketChartEntry constructor(
	val minEntry: Entry?,
	val maxEntry: Entry?,
	val entries: List<Entry>
)
