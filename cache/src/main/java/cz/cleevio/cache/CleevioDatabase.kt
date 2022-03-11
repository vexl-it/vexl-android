package cz.cleevio.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import cz.cleevio.cache.dao.UserDao
import cz.cleevio.cache.entity.UserEntity

@Database(
	entities = [
		UserEntity::class
	],
	version = 1,
	// Export is true only if you want to create new json schema for testing purpose
	exportSchema = false
)
//@TypeConverters(DatabaseTypeConverters::class)
abstract class CleevioDatabase : RoomDatabase() {

	abstract fun userDao(): UserDao
}