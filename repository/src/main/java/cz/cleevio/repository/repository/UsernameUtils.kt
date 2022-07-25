package cz.cleevio.repository.repository

object UsernameUtils {

	const val NAME_REPEAT_COUNT = 3
	private val options: List<String> = listOf(
		"bo",
		"da",
		"ga", "ge",
		"chi",
		"ka", "ko", "ku",
		"ma", "mi", "mo",
		"na", "no",
		"ro", "ri", "ru",
		"sa", "se", "su",
		"ta", "te", "to",
		"yu",
		"za", "zo"
	)

	fun generateName(): String {
		var name = options.random()
		repeat(NAME_REPEAT_COUNT) {
			name = name.plus(options.random())
		}
		return name.replaceFirstChar(Char::titlecase)
	}
}