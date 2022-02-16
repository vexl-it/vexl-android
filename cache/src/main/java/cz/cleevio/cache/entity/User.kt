package cz.cleevio.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User constructor(
	@PrimaryKey val id: String,
	val email: String,
	val name: String,
	val surname: String,
	val photoUrl: String,
	val role: List<Role>
) {

	data class Role constructor(
		val id: Long,
		val name: String
	)

	fun getFullname(): String = "$name $surname"

	override fun toString(): String =
		"User(id='$id', email='$email', name='$name', surname='$surname', role=$role)"
}