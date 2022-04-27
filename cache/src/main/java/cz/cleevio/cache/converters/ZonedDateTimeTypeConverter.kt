package cz.cleevio.cache.converters

import androidx.room.TypeConverter
import java.time.ZonedDateTime

class ZonedDateTimeTypeConverter {

	@TypeConverter
	fun toDate(dateString: String?): ZonedDateTime? {
		return if (dateString == null) {
			null
		} else {
			ZonedDateTime.parse(dateString)
		}
	}

	@TypeConverter
	fun toDateString(date: ZonedDateTime?): String? = date?.toString()
}