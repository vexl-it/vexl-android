package cz.cleevio.network.response.common

import java.math.BigDecimal

data class EncryptedBigDecimal(
	val decryptedValue: BigDecimal
)