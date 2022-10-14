package cz.cleevio.network.response.offer.v2

import com.squareup.moshi.JsonClass
import cz.cleevio.network.response.common.EncryptedString

@JsonClass(generateAdapter = true)
data class OfferUnifiedResponseV2 constructor(
	val offerId: String,
	val publicPayload: String,
	val privatePayload: EncryptedString,
	val expiration: Long,
	val createdAt: String,
	val modifiedAt: String,
)
