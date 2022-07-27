package lightbase.camera.utils

import androidx.camera.core.AspectRatio
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object CameraUtils {

	fun aspectRatio(width: Int, height: Int): Int {
		val previewRatio = max(width, height).toDouble() / min(width, height)
		if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
			return AspectRatio.RATIO_4_3
		}
		return AspectRatio.RATIO_16_9
	}

	private const val RATIO_4_3_VALUE = 4.0 / 3.0
	private const val RATIO_16_9_VALUE = 16.0 / 9.0
}