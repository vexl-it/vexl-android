package cz.cleevio.network.request.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateOfferRequest constructor(
	val offerPrivateList: List<CreateOfferPrivateRequest>,
	val expiration: Long
)

@JsonClass(generateAdapter = true)
data class CreateOfferPrivateRequest constructor(
	val userPublicKey: String,
	val offerPublicKey: String,
	val location: List<String>,
	val offerDescription: String,
	val amountBottomLimit: String,
	val amountTopLimit: String,
	val feeState: String,
	val feeAmount: String,
	val locationState: String,
	val paymentMethod: List<String>,
	val btcNetwork: List<String>,
	val friendLevel: String,
	val offerType: String
)
