package cz.cleevio.core.utils

import android.content.Context
import android.graphics.drawable.Drawable
import cz.cleevio.core.R
import cz.cleevio.repository.repository.UsernameUtils

object RandomUtils {

	val imageOptions: List<Int> = listOf(
		R.drawable.ic_baseline_person_128,
		R.drawable.ic_baseline_person_128,
	)

	fun generateName(): String = UsernameUtils.generateName()

	//todo: expand when we receive icons
	fun selectRandomImage(context: Context): Drawable? = context.getDrawable(imageOptions.random())
}