package cz.cleevio.network.response.offer

import com.squareup.moshi.JsonClass
import cz.cleevio.network.response.common.EncryptedBigDecimal
import cz.cleevio.network.response.common.EncryptedString

@JsonClass(generateAdapter = true)
data class OfferUnifiedResponse constructor(
	val offerId: EncryptedString,
	val location: EncryptedString,
	val userPublicKey: String,
	val offerPublicKey: EncryptedString,
	val offerDescription: EncryptedString,
	val amountBottomLimit: EncryptedBigDecimal,
	val amountTopLimit: EncryptedBigDecimal,
	val feeState: EncryptedString,
	val feeAmount: EncryptedBigDecimal,
	val locationState: EncryptedString,
	val paymentMethod: EncryptedString,
	val btcNetwork: EncryptedString,
	val friendLevel: EncryptedString,
	val createdAt: EncryptedString,
	val modifiedAt: EncryptedString
)