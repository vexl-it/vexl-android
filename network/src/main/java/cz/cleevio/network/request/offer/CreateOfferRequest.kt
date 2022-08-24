package cz.cleevio.network.request.offer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateOfferRequest constructor(
	val offerPrivateList: List<OfferPrivateCreateRequest>,
	val expiration: Long,
	val offerType: String
)

@JsonClass(generateAdapter = true)
data class OfferPrivateCreateRequest constructor(
	val userPublicKey: String,
	val location: List<String>,
	val offerPublicKey: String,
	val offerDescription: String,
	val amountBottomLimit: String,
	val amountTopLimit: String,
	val feeState: String,
	val feeAmount: String,
	val locationState: String,
	val paymentMethod: List<String>,
	val btcNetwork: List<String>,
	val currency: String,
	val friendLevel: String,
	val offerType: String,
	val activePriceState: String,
	val activePriceValue: String,
	val active: String,
	val groupUuid: String,
	val commonFriends: List<String>,
)