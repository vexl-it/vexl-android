package lightbase.camera.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ImageHelper : KoinComponent {

	private val imageLoader by inject<ImageLoader>()

	@SuppressLint("QueryPermissionsNeeded")
	fun createCameraIntent(
		fragmentActivity: FragmentActivity,
		frontFaceCamera: Boolean = false,
		intentMessage: String
	): Pair<Intent, Uri> {
		val intents = ArrayList<Intent>()
		val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

		val uri = getImagePathUriForCamera(fragmentActivity)

		for (info in fragmentActivity.packageManager.queryIntentActivities(captureIntent, 0)) {
			val packageName = info.activityInfo.packageName
			val intent = Intent(captureIntent)
			intent.component = (ComponentName(packageName, info.activityInfo.name))
			intent.setPackage(packageName)
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
			if (frontFaceCamera) {
				intent.setFrontCameraAsDefault()
			}
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			intents.add(intent)
		}
		val pickIntent = Intent().apply {
			type = "*/*"
			addCategory(Intent.CATEGORY_OPENABLE)
			putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
			putExtra(Intent.EXTRA_LOCAL_ONLY, true)
			action = Intent.ACTION_OPEN_DOCUMENT
		}
		val intent = Intent.createChooser(pickIntent, intentMessage).apply {
			putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
		}

		return Pair(intent, uri)
	}

	fun openCameraIntent(
		fragmentActivity: FragmentActivity,
		frontFaceCamera: Boolean = false,
	): Pair<Intent, Uri> {
		val uri = getImagePathUriForCamera(fragmentActivity)

		val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
			.putExtra(MediaStore.EXTRA_OUTPUT, uri)
			.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

		if (frontFaceCamera) {
			intent.setFrontCameraAsDefault()
		}

		return Pair(intent, uri)
	}

	fun createGalleryIntent(): Intent {
		val photoPickerIntent = Intent(Intent.ACTION_PICK)
		photoPickerIntent.type = "image/*"
		return photoPickerIntent
	}

	suspend fun loadImage(activity: FragmentActivity, uri: Uri, width: Int = 500, height: Int = 500): Bitmap? {
		return suspendCoroutine { cont ->
			val request = ImageRequest.Builder(activity)
				.data(uri)
				.size(width, height)
				.target(
					onError = {
						Timber.e("Cannot load an image $uri")
						cont.resume(null)
					},
					onSuccess = { drawable ->
						val bitmap = drawable as BitmapDrawable
						cont.resume(bitmap.bitmap)
					})
				.build()

			imageLoader.enqueue(request)
		}
	}

	@Suppress("DEPRECATION")
	suspend fun shareViaIntent(activity: FragmentActivity, imageUri: Uri, intentMessage: String) {
		withContext(Dispatchers.Main) {
			val shareIntent: Intent = Intent().apply {
				action = Intent.ACTION_SEND
				flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
				setDataAndType(imageUri, activity.contentResolver.getType(imageUri))
				putExtra(Intent.EXTRA_STREAM, imageUri)
				type = "image/jpeg"
			}

			activity.startActivity(Intent.createChooser(shareIntent, intentMessage))
		}
	}

	private fun Intent.setFrontCameraAsDefault(): Intent {
		val sdk = Build.VERSION.SDK_INT
		return when {
			sdk >= Build.VERSION_CODES.LOLLIPOP_MR1 && sdk < Build.VERSION_CODES.O -> {
				putExtra("android.intent.extras.CAMERA_FACING", CameraCharacteristics.LENS_FACING_FRONT)
			}
			sdk >= Build.VERSION_CODES.O -> {
				putExtra("android.intent.extras.CAMERA_FACING", CameraCharacteristics.LENS_FACING_FRONT)
				putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
			}
			else -> putExtra("android.intent.extras.CAMERA_FACING", 1)
		}
	}

	@Suppress("deprecation")
	fun getImagePathUriForCamera(context: Context): Uri {
		val ext = context.getExternalFilesDir(null)
		var path = if (ext != null) {
			ext.absolutePath
		} else {
			Environment.getExternalStorageDirectory().absolutePath
		}
		path += "/images"
		val directory = File(path)
		directory.mkdirs()
		val imageFile = File(directory, "image_${System.currentTimeMillis()}.png")

		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			FileProvider.getUriForFile(
				context,
				context.packageName + ".fileprovider",
				imageFile
			)
		} else {
			Uri.fromFile(imageFile)
		}
	}
}