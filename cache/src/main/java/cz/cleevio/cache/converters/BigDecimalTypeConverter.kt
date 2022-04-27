package cz.cleevio.cache.converters

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalTypeConverter {

	@TypeConverter
	fun bigDecimalToString(input: BigDecimal?): String? = input?.toPlainString()

	@TypeConverter
	fun stringToBigDecimal(input: String?): BigDecimal? = input?.toBigDecimalOrNull()
}