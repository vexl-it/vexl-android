package cz.cleevio.cache

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import cz.cleevio.cache.entity.User

class DatabaseTypeConverters {
	private val moshi = Moshi.Builder().build()

	@TypeConverter
	fun listUserRoleFromString(data: String?): List<User.Role>? {
		if (data == null) return null
		val type = Types.newParameterizedType(List::class.java, User.Role::class.java)
		val adapter = moshi.adapter<List<User.Role>>(type)
		return adapter.fromJson(data)
	}

	@TypeConverter
	fun listUserRoleToString(data: List<User.Role>?): String? {
		if (data == null) return null
		val type = Types.newParameterizedType(List::class.java, User.Role::class.java)
		val adapter = moshi.adapter<List<User.Role>>(type)
		return adapter.toJson(data)
	}
}