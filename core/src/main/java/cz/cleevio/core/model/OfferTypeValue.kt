package cz.cleevio.core.model

data class OfferTypeValue(
	val value: OfferType
)

enum class OfferType {
	BUY,
	SELL;

	fun isBuy() = this == BUY
}