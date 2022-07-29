package cz.cleevio.profile.cameraFragment.mlVision

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.mlkit.vision.common.InputImage
import timber.log.Timber

abstract class VisionProcessorBase<T> : VisionImageProcessor {

	private val executor = ScopedExecutor(TaskExecutors.MAIN_THREAD)

	override fun processBitmap(bitmap: Bitmap?) {
		requestDetectInImage(InputImage.fromBitmap(bitmap!!, 0))
	}

	private fun requestDetectInImage(image: InputImage): Task<T> = setUpListener(detectInImage(image))

	private fun setUpListener(task: Task<T>): Task<T> =
		task
			.addOnSuccessListener(
				executor, { results: T ->
					this@VisionProcessorBase.onSuccess(results)
				}
			)
			.addOnFailureListener(
				executor, { e: Exception ->
					Timber.e(e)
					this@VisionProcessorBase.onFailure(e)
				}
			)

	override fun stop() {
		executor.shutdown()
	}

	protected abstract fun detectInImage(image: InputImage): Task<T>
	protected abstract fun onSuccess(results: T)
	protected abstract fun onFailure(e: Exception)
}
