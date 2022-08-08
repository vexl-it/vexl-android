package cz.cleevio.core.utils

import android.content.Context
import android.graphics.drawable.Drawable
import cz.cleevio.core.R
import cz.cleevio.repository.repository.UsernameUtils

object RandomUtils {

	private val imageOptions: List<Int> = listOf(
		R.drawable.random_avatar_1,
		R.drawable.random_avatar_2,
		R.drawable.random_avatar_3,
		R.drawable.random_avatar_4,
		R.drawable.random_avatar_5,
		R.drawable.random_avatar_6
	)

	fun generateName(): String = UsernameUtils.generateName()

	//todo: expand when we receive icons
	fun selectRandomImage(context: Context): Drawable? = context.getDrawable(imageOptions.random())
}
