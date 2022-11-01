package cz.cleevio.core.utils

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.load
import coil.loadAny
import cz.cleevio.repository.RandomUtils

fun setUserAvatar(imageView: ImageView, avatar: Any?, anonymousAvatarImageIndex: Int?, context: Context) {
	if (avatar == null) {
		if (anonymousAvatarImageIndex != null) {
			imageView.load(
				drawableResId = RandomUtils.getRandomImageDrawableId(anonymousAvatarImageIndex),
				imageLoader = ImageLoader.invoke(context)
			) {
				setPlaceholders(cz.cleevio.core.R.drawable.random_avatar_3)
			}
		} else {
			imageView.load(cz.cleevio.core.R.drawable.random_avatar_3, imageLoader = ImageLoader.invoke(context))
		}
	} else {
		imageView.loadAny(avatar) {
			setPlaceholders(cz.cleevio.core.R.drawable.random_avatar_3)
		}
	}
}