package cz.cleevio.network.response.offer

import com.squareup.moshi.JsonClass
import cz.cleevio.network.response.common.EncryptedBigDecimal
import cz.cleevio.network.response.common.EncryptedLocation
import cz.cleevio.network.response.common.EncryptedString

@JsonClass(generateAdapter = true)
data class OfferUnifiedResponse constructor(
	val offerId: String,
	val location: List<EncryptedLocation>,
	val userPublicKey: String,
	val offerPublicKey: EncryptedString,
	val offerDescription: EncryptedString,
	val amountBottomLimit: EncryptedBigDecimal,
	val amountTopLimit: EncryptedBigDecimal,
	val feeState: EncryptedString,
	val feeAmount: EncryptedBigDecimal,
	val locationState: EncryptedString,
	val paymentMethod: List<EncryptedString>,
	val btcNetwork: List<EncryptedString>,
	val friendLevel: EncryptedString,
	val offerType: EncryptedString,
	val activePriceState: EncryptedString,
	val activePriceValue: EncryptedBigDecimal,
	val active: EncryptedString,
	val commonFriends: List<EncryptedString>,
	val groupUuid: EncryptedString,
	val createdAt: String,
	val modifiedAt: String
)