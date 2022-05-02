package cz.cleevio.network.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import cz.cleevio.network.response.offer.LocationResponse

class LocationAdapter {

	@FromJson
	fun fromJson(data: String): LocationResponse {
		val moshi = Moshi
			.Builder()
			.add(BigDecimalAdapter)
			.build()
		return moshi.adapter(LocationResponse::class.java).fromJson(data)!!
	}

	@ToJson
	fun toJson(value: LocationResponse): String {
		val moshi = Moshi
			.Builder()
			.add(BigDecimalAdapter)
			.build()
		return moshi.adapter(LocationResponse::class.java).toJson(value)
	}
}