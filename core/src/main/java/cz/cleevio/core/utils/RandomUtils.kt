package cz.cleevio.core.utils

import android.content.Context
import android.graphics.drawable.Drawable
import cz.cleevio.core.R
import cz.cleevio.repository.repository.UsernameUtils
import java.lang.Math.random
import java.util.*
import kotlin.random.Random

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

	fun getAvatarIndex(): Int = Random.nextInt(0, imageOptions.size)

	fun getRandomImageDrawableId(avatarIndex: Int): Int = imageOptions[avatarIndex]

	//todo: expand when we receive icons
	fun selectRandomImage(context: Context, drawableInt: Int): Drawable? = context.getDrawable(drawableInt)
}
