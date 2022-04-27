package cz.cleevio.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import cz.cleevio.cache.dao.ContactDao
import cz.cleevio.cache.dao.ContactKeyDao
import cz.cleevio.cache.dao.MyOfferDao
import cz.cleevio.cache.dao.UserDao
import cz.cleevio.cache.entity.ContactEntity
import cz.cleevio.cache.entity.ContactKeyEntity
import cz.cleevio.cache.entity.MyOfferEntity
import cz.cleevio.cache.entity.UserEntity

@Database(
	entities = [
		UserEntity::class,
		ContactEntity::class,
		ContactKeyEntity::class,
		MyOfferEntity::class
	],
	version = 1,
	// Export is true only if you want to create new json schema for testing purpose
	exportSchema = false
)
//@TypeConverters(DatabaseTypeConverters::class)
abstract class CleevioDatabase : RoomDatabase() {

	abstract fun userDao(): UserDao
	abstract fun contactDao(): ContactDao
	abstract fun contactKeyDao(): ContactKeyDao
	abstract fun myOfferDao(): MyOfferDao
}