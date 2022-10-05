package cz.cleevio.network.request.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReportOfferRequest constructor(
	val offerId: String
)
