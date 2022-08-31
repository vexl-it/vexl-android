package cz.cleevio.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.cleevio.cache.entity.CryptoCurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoCurrencyDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun replace(cryptoCurrencyEntity: CryptoCurrencyEntity): Long

	@Query("SELECT * FROM CryptoCurrencyEntity LIMIT 1")
	fun getCryptoCurrencyFlow(): Flow<CryptoCurrencyEntity?>

	@Query("SELECT * FROM CryptoCurrencyEntity LIMIT 1")
	fun getCryptoCurrency(): CryptoCurrencyEntity?
}