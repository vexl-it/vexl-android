package cz.cleevio.core.utils

import android.graphics.Bitmap
import android.util.Base64
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.base.BaseAvatarViewModel
import cz.cleevio.core.base.SIZE_LIMIT
import cz.cleevio.repository.repository.user.UserRepository
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

//this class should be deleted when not needed
class TempUtils constructor(
	val userRepository: UserRepository,
	val encryptedPreferenceRepository: EncryptedPreferenceRepository,
) {

	//convert big profile picture to smaller profile picture.
	//Added in build 1.0.11
	//FIXME: should be removed in next build
	suspend fun convertAvatarToSmaller() {
		val user = userRepository.getUser() ?: return
		var avatarBitmap = user.avatarBase64?.getBitmap() ?: return
		val biggerSize = Integer.max(avatarBitmap.width, avatarBitmap.height).toFloat()
		if (biggerSize > SIZE_LIMIT) {
			val ratio: Float = SIZE_LIMIT / biggerSize
			avatarBitmap = Bitmap.createScaledBitmap(
				avatarBitmap,
				(avatarBitmap.width.toFloat() * ratio).roundToInt(),
				(avatarBitmap.height.toFloat() * ratio).roundToInt(),
				false
			)
		}

		val baos = ByteArrayOutputStream()
		avatarBitmap.compress(BaseAvatarViewModel.COMPRESS_FORMAT, BaseAvatarViewModel.BITMAP_COMPRESS_QUALITY, baos)

		val encodedSmallerAvatar = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT or Base64.NO_WRAP)
		userRepository.updateUser(
			avatar = encodedSmallerAvatar
		)

		encryptedPreferenceRepository.hasConvertedAvatarToSmallerSize = true
	}
}