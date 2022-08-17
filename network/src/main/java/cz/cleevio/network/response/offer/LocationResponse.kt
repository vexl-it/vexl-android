package cz.cleevio.network.response.offer

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class LocationResponse constructor(
	val latitude: BigDecimal,
	val longitude: BigDecimal,
	val radius: BigDecimal?,
	val city: String?
)
