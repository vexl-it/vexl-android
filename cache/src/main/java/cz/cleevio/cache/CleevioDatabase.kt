package cz.cleevio.cache

import androidx.room.AutoMigration
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
		ReportedOfferEntity::class,
		LocationEntity::class,
		ChatContactEntity::class,
		ChatMessageEntity::class,
		NotificationEntity::class,
		GroupEntity::class,
		ChatUserIdentityEntity::class,
		CryptoCurrencyEntity::class,
		ChatEntity::class,
	],
	version = 7,
	autoMigrations = [
		AutoMigration(from = 1, to = 2),
		AutoMigration(from = 2, to = 3),
		AutoMigration(from = 3, to = 4),
		AutoMigration(from = 4, to = 5),
		AutoMigration(from = 5, to = 6),
		AutoMigration(from = 6, to = 7),
	],
	exportSchema = true
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
	abstract fun reportedOfferDao(): ReportedOfferDao
	abstract fun offerCommonFriendCrossRefDao(): OfferCommonFriendCrossRefDao
	abstract fun groupDao(): GroupDao
	abstract fun cryptoCurrencyDao(): CryptoCurrencyDao
	abstract fun chatDao(): ChatDao
}