package lightbase.camera.ui.takePhotoFragment

import android.os.Bundle
import androidx.core.os.bundleOf

sealed class TakePhotoResult {

	data class Success constructor(
		val url: String? = null
	) : TakePhotoResult()

	object Denied : TakePhotoResult()
	object PermanentlyDenied : TakePhotoResult()
	object ManuallyClosed : TakePhotoResult()

	companion object {
		private const val PARAM_URL = "param_url"
		private const val PARAM_STATE = "state"
		private const val PARAM_STATE_SUCCESS = "success"
		private const val PARAM_STATE_DENIED = "denied"
		private const val PARAM_STATE_PERMANENTLY_DENIED = "permanently_denied"
		private const val PARAM_STATE_CLOSED = "closed"

		fun fromBundle(bundle: Bundle): TakePhotoResult {
			return when (bundle.getString(PARAM_STATE)) {
				PARAM_STATE_SUCCESS -> {
					Success(bundle.getString(PARAM_URL, ""))
				}
				PARAM_STATE_DENIED -> {
					Denied
				}
				PARAM_STATE_PERMANENTLY_DENIED -> {
					PermanentlyDenied
				}
				PARAM_STATE_CLOSED -> {
					ManuallyClosed
				}
				else -> Denied
			}
		}

		fun toBundle(takePhotoResult: TakePhotoResult): Bundle {
			return when (takePhotoResult) {
				is Success -> {
					bundleOf(
						PARAM_STATE to PARAM_STATE_SUCCESS,
						PARAM_URL to takePhotoResult.url
					)
				}
				Denied -> {
					bundleOf(PARAM_STATE to PARAM_STATE_DENIED)
				}
				PermanentlyDenied -> {
					bundleOf(PARAM_STATE to PARAM_STATE_PERMANENTLY_DENIED)
				}
				ManuallyClosed -> {
					bundleOf(PARAM_STATE to PARAM_STATE_CLOSED)
				}
			}
		}
	}
}