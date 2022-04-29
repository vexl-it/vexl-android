package cz.cleevio.network.response.common

import cz.cleevio.network.response.offer.LocationResponse

data class EncryptedLocation(
	val decryptedValue: LocationResponse
)
