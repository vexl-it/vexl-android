package cz.cleeevio.onboarding.avatarFragment

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.repository.model.user.UserRegistration
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lightbase.camera.ui.takePhotoFragment.TakePhotoResult
import lightbase.core.baseClasses.BaseViewModel
import timber.log.Timber
import java.io.ByteArrayOutputStream

class AvatarViewModel constructor(
	private val userRepository: UserRepository,
	val navMainGraphModel: NavMainGraphModel
) : BaseViewModel() {

	private val _userRegistration = MutableStateFlow<UserRegistration?>(null)
	val userRegistration = _userRegistration.asStateFlow()

	private val _profileImageUri = MutableStateFlow<Uri?>(null)
	val profileImageUri = _profileImageUri.asStateFlow()

	fun registerUser(
		username: String,
		contentResolver: ContentResolver
	) {
		viewModelScope.launch(Dispatchers.IO) {
			profileImageUri.value?.let {
				val response = userRepository.registerUser(
					username,
					getAvatarData(it, contentResolver)
				)
				_userRegistration.emit(response.data)
			}
		}
	}

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

	private fun getAvatarData(avatarUri: Uri, contentResolver: ContentResolver): String {
		val bitmap = getBitmap(avatarUri, contentResolver)

		val baos = ByteArrayOutputStream()
		bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
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
}