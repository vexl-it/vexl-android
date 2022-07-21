package cz.cleevio.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.cleevio.cache.converters.BigDecimalTypeConverter
import cz.cleevio.cache.converters.ZonedDateTimeTypeConverter
import cz.cleevio.cache.dao.*
import cz.cleevio.cache.entity.*

@Database(
	entities = [
		UserEntity::class,
		ContactEntity::class,
		ContactKeyEntity::class,
		MyOfferEntity::class,
		OfferEntity::class,
		OfferCommonFriendCrossRef::class,
		RequestedOfferEntity::class,
		LocationEntity::class,
		ChatContactEntity::class,
		ChatMessageEntity::class,
		NotificationEntity::class,
		GroupEntity::class,
		ChatUserEntity::class
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
	abstract fun locationDao(): LocationDao
	abstract fun chatMessageDao(): ChatMessageDao
	abstract fun chatContactDao(): ChatContactDao
	abstract fun chatUserDao(): ChatUserDao
	abstract fun notificationDao(): NotificationDao
	abstract fun requestedOfferDao(): RequestedOfferDao
	abstract fun offerCommonFriendCrossRefDao(): OfferCommonFriendCrossRefDao
	abstract fun groupDao(): GroupDao
}