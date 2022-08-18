package cz.cleevio.repository.model.offer

data class OfferFilter constructor(
	val locationType: String? = null,
	val locations: List<Location>? = null,
	val paymentMethods: Set<String>? = null,
	val btcNetworks: Set<String>? = null,
	val friendLevels: Set<String>? = null,
	val feeType: String? = null,
	val feeValue: Float? = null,
	val priceRangeTopLimit: Float? = null,
	val priceRangeBottomLimit: Float? = null,
	val currency: String? = null
) {

	fun isOfferMatchPriceRange(minOffer: Float, maxOffer: Float): Boolean {
		val minFilter = priceRangeBottomLimit ?: return true
		val maxFilter = priceRangeTopLimit ?: return true
		return (maxOffer in minFilter..maxFilter)
			|| (minOffer in minFilter..maxFilter)
			|| (minFilter in minOffer..maxOffer)
			|| (maxFilter in minOffer..maxOffer)
	}

	fun isOfferLocationInRadius(offerLocations: List<Location>): Boolean {
		if (locations.isNullOrEmpty()) return true
		if (offerLocations.isEmpty()) return false

		locations.forEach { location ->
			offerLocations.forEach { offerLocation ->
				if (location.city.equals(offerLocation.city, true)) return true
				/* TODO will be supported in future
				if (location.latitude == offerLocation.latitude && location.longitude == location.longitude) return true

				val result = floatArrayOf(0f)
				android.location.Location.distanceBetween(
					location.latitude.toDouble(),
					location.longitude.toDouble(),
					offerLocation.latitude.toDouble(),
					offerLocation.longitude.toDouble(),
					result
				)
				val distanceInKm = result[POSITION_OF_DISTANCE] / ONE_KM_IN_METERS
				val radius = (location.radius + offerLocation.radius).toFloat()
				if (distanceInKm <= radius) return true*/
			}
		}

		return false
	}

	fun isFilterInUse() =
		locationType != null ||
			locations?.isNotEmpty() == true ||
			paymentMethods?.isNotEmpty() == true ||
			btcNetworks?.isNotEmpty() == true ||
			friendLevels?.isNotEmpty() == true ||
			feeType != null ||
			feeValue != null ||
			priceRangeTopLimit != null ||
			priceRangeBottomLimit != null ||
			currency != null
}

const val POSITION_OF_DISTANCE = 0
const val ONE_KM_IN_METERS = 1000