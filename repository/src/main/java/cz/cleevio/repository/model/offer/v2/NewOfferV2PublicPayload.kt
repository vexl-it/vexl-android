package cz.cleevio.repository.model.offer.v2

import com.squareup.moshi.JsonClass

//this class will be transformed to JSON, serialized as String and encrypted
@JsonClass(generateAdapter = true)
data class NewOfferV2PublicPayload constructor(
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
	val currency: String,
	val offerType: String,
	val activePriceState: String,
	val activePriceValue: String,
	val activePriceCurrency: String,
	val active: String,
	val groupUuids: List<String>
)
