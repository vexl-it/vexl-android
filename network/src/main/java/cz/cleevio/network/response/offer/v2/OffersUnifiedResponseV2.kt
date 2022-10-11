package cz.cleevio.network.response.offer.v2

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OffersUnifiedResponseV2 constructor(
	val offers: List<OfferUnifiedResponseV2>
)
