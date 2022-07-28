package cz.cleevio.repository.model.contact

enum class ContactLevel {
	FIRST,
	SECOND,
	GROUP,
	NOT_SPECIFIED
}

fun cz.cleevio.cache.entity.ContactLevel.fromCache(): ContactLevel {
	return when (this) {
		cz.cleevio.cache.entity.ContactLevel.FIRST -> ContactLevel.FIRST
		cz.cleevio.cache.entity.ContactLevel.SECOND -> ContactLevel.SECOND
		cz.cleevio.cache.entity.ContactLevel.NOT_SPECIFIED -> ContactLevel.NOT_SPECIFIED
		cz.cleevio.cache.entity.ContactLevel.GROUP -> ContactLevel.GROUP
	}
}

fun ContactLevel.toCache(): cz.cleevio.cache.entity.ContactLevel {
	return when (this) {
		ContactLevel.FIRST -> cz.cleevio.cache.entity.ContactLevel.FIRST
		ContactLevel.SECOND -> cz.cleevio.cache.entity.ContactLevel.SECOND
		ContactLevel.NOT_SPECIFIED -> cz.cleevio.cache.entity.ContactLevel.NOT_SPECIFIED
		ContactLevel.GROUP -> cz.cleevio.cache.entity.ContactLevel.GROUP
	}
}