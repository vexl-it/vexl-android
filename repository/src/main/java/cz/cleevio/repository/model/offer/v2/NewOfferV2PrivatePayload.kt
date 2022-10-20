package cz.cleevio.repository.model.offer.v2

import com.squareup.moshi.JsonClass

//this class will be transformed to JSON, serialized as String and encrypted
@JsonClass(generateAdapter = true)
data class NewOfferV2PrivatePayload constructor(
	val commonFriends: List<String>,
	val friendLevel: List<String>,
	val symetricKey: String
)
