package cz.cleevio.network.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import cz.cleevio.network.response.offer.LocationResponse

class LocationAdapter {

	@FromJson
	fun fromJson(encryptedData: String): LocationResponse {
		val moshi = Moshi.Builder().build()
		return moshi.adapter(LocationResponse::class.java).fromJson(encryptedData)!!
	}

	@ToJson
	fun toJson(value: LocationResponse): String {
		val moshi = Moshi.Builder().build()
		return moshi.adapter(LocationResponse::class.java).toJson(value)
	}
}