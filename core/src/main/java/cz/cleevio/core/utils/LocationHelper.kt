package cz.cleevio.core.utils

import com.squareup.moshi.Moshi
import cz.cleevio.repository.model.offer.Location

class LocationHelper constructor(
	private val moshi: Moshi
) {
	fun locationToJsonString(location: Location): String = moshi.adapter(Location::class.java).toJson(location)
}