package cz.cleevio.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.cleevio.cache.converters.BigDecimalTypeConverter
import cz.cleevio.cache.converters.ZonedDateTimeTypeConverter
import cz.cleevio.cache.dao.ContactDao
import cz.cleevio.cache.dao.ContactKeyDao
import cz.cleevio.cache.dao.MyOfferDao
import cz.cleevio.cache.dao.OfferDao
import cz.cleevio.cache.dao.UserDao
import cz.cleevio.cache.entity.ContactEntity
import cz.cleevio.cache.entity.ContactKeyEntity
import cz.cleevio.cache.entity.MyOfferEntity
import cz.cleevio.cache.entity.OfferEntity
import cz.cleevio.cache.entity.UserEntity

@Database(
	entities = [
		UserEntity::class,
		ContactEntity::class,
		ContactKeyEntity::class,
		MyOfferEntity::class,
		OfferEntity::class
	],
	version = 1,
	// Export is true only if you want to create new json schema for testing purpose
	exportSchema = false
)
@TypeConverters(
	BigDecimalTypeConverter::class,
	ZonedDateTimeTypeConverter::class
)
abstract class CleevioDatabase : RoomDatabase() {

	abstract fun userDao(): UserDao
	abstract fun contactDao(): ContactDao
	abstract fun contactKeyDao(): ContactKeyDao
	abstract fun myOfferDao(): MyOfferDao
	abstract fun offerDao(): OfferDao
}