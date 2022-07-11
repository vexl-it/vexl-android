package cz.cleevio.network.request.market

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MarketChartRequest constructor(
	val from: String,
	val to: String,
	val currency: String
)