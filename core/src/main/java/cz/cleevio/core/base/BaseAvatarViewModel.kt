package cz.cleevio.core.base

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lightbase.camera.ui.takePhotoFragment.TakePhotoResult
import lightbase.camera.utils.ImageHelper
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.lang.Integer.max
import kotlin.math.roundToInt

const val SIZE_LIMIT = 1024f

abstract class BaseAvatarViewModel constructor(
	val navMainGraphModel: NavMainGraphModel,
	val imageHelper: ImageHelper
) : BaseViewModel() {

	private val _profileImageUri = MutableStateFlow<Uri?>(null)
	val profileImageUri = _profileImageUri.asStateFlow()

	fun processTakingPhotoResult(result: TakePhotoResult) {
		Timber.d("taking photo result $result")

		when (result) {
			is TakePhotoResult.Success -> {
				result.url?.let {
					viewModelScope.launch {
						_profileImageUri.emit(Uri.parse(it))
					}
				}
			}
			is TakePhotoResult.Denied -> {
			}
			is TakePhotoResult.ManuallyClosed -> {
			}
			is TakePhotoResult.PermanentlyDenied -> {
			}
		}
	}

	//return Base64 representation of image
	protected fun getAvatarData(avatarUri: Uri, contentResolver: ContentResolver): String {
		var bitmap = getBitmap(avatarUri, contentResolver)

		val biggerSize = max(bitmap.width, bitmap.height).toFloat()
		if (biggerSize > SIZE_LIMIT) {
			val ratio: Float = SIZE_LIMIT / biggerSize
			bitmap = Bitmap.createScaledBitmap(
				bitmap,
				(bitmap.width.toFloat() * ratio).roundToInt(),
				(bitmap.height.toFloat() * ratio).roundToInt(),
				false
			)
		}

		val baos = ByteArrayOutputStream()
		bitmap.compress(COMPRESS_FORMAT, BITMAP_COMPRESS_QUALITY, baos)
		return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT or Base64.NO_WRAP)
	}

	private fun getBitmap(avatarUri: Uri, contentResolver: ContentResolver): Bitmap {
		return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
			MediaStore.Images.Media.getBitmap(
				contentResolver,
				avatarUri
			)
		} else {
			val source = ImageDecoder.createSource(contentResolver, avatarUri)
			ImageDecoder.decodeBitmap(source)
		}
	}

	fun updateProfileUri(uri: Uri) {
		viewModelScope.launch {
			_profileImageUri.emit(uri)
		}
	}

	companion object {
		const val BITMAP_COMPRESS_QUALITY = 50
		const val IMAGE_EXTENSION = "jpg"
		val COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG
	}
}