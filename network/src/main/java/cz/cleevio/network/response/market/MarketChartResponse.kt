package cz.cleevio.network.response.market

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MarketChartResponse constructor(
	val prices: List<List<Double>>
)