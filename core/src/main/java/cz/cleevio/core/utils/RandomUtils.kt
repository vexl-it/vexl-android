package cz.cleevio.core.utils

import android.content.Context
import android.graphics.drawable.Drawable
import cz.cleevio.core.R


object RandomUtils {

	const val NAME_REPEAT_COUNT = 3
	val options: List<String> = mutableListOf(
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

	//todo: expand when we receive icons
	fun selectRandomImage(context: Context): Drawable? {
		return context.getDrawable(R.drawable.ic_baseline_person_128)
	}

}