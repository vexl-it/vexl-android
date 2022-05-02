package cz.cleevio.repository.model.offer

import com.squareup.moshi.JsonClass
import cz.cleevio.cache.entity.LocationEntity
import cz.cleevio.network.response.offer.LocationResponse
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class Location constructor(
	val longitude: BigDecimal,
	val latitude: BigDecimal,
	val radius: BigDecimal
)

fun LocationResponse.fromNetwork(): Location {
	return Location(
		longitude = this.longitude,
		latitude = this.latitude,
		radius = this.radius
	)
}

fun LocationEntity.fromCache(): Location {
	return Location(
		longitude = this.longitude,
		latitude = this.latitude,
		radius = this.radius
	)
}

fun Location.toCache(offerId: Long): LocationEntity {
	return LocationEntity(
		offerId = offerId,
		longitude = this.longitude,
		latitude = this.latitude,
		radius = this.radius
	)
}
