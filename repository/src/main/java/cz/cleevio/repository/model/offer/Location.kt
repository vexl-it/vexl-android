package cz.cleevio.repository.model.offer

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class Location constructor(
	val longitude: BigDecimal,
	val latitude: BigDecimal,
	val radius: BigDecimal
)
