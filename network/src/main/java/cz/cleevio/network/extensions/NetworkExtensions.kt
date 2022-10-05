package cz.cleevio.network.extensions

import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.util.Base64
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import cz.cleevio.network.NetworkError
import cz.cleevio.network.R
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.response.BaseResponseJsonAdapter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

@Suppress("NestedBlockDepth", "SwallowedException", "LongMethod")
suspend fun <E, O> tryOnline(
	doOnSuccess: suspend ((O?) -> Unit) = {},
	doOnError: suspend ((Int, Int?) -> ErrorIdentification?) = { _, _ -> null },
	mapper: (E?) -> O?,
	request: suspend () -> Response<E>
): Resource<O> {
	val networkError = object : KoinComponent {
		val networkError: NetworkError by inject()
	}.networkError
	var error: ErrorIdentification? = null
	return try {
		val response = request()
		if (response.isSuccessful) {
			val mappedResponse = mapResource(
				Resource.success(response.body()),
				mapper
			)
			doOnSuccess(mappedResponse.data)
			mappedResponse
		} else {
			val baseResponse = try {
				response.errorBody()
					?.source()
					?.let {
						BaseResponseJsonAdapter(
							moshi = Moshi.Builder().build()
						).fromJson(it)
					}
				//hack to prevent crash in case of missing response body
			} catch (e: EOFException) {
				null
			}

			val firstMessage = baseResponse?.message?.firstOrNull()
			val resource = if (firstMessage != null) {
				Resource.error<O>(
					errorIdentification = ErrorIdentification.StringMessageError(stringMessage = firstMessage)
				)
			} else {
				val subcode = baseResponse?.code
					?.toIntOrNull()
				Resource.error<O>(code = response.code(), subcode = subcode)
			}

			error = doOnError(response.code(), resource.errorIdentification.subcode) ?: resource.errorIdentification
			resource
		}
	} catch (e: HttpException) {
		Timber.e(e)
		error = doOnError(e.code(), null) ?: ErrorIdentification.Unknown(e.code(), null)
		Resource.errorUnknown(code = e.code())
	} catch (e: SocketTimeoutException) {
		Timber.e(e)
		error = ErrorIdentification.ConnectionProblem
		Resource.error(error)
	} catch (e: UnknownHostException) {
		Timber.e(e)
		error = ErrorIdentification.ConnectionProblem
		Resource.error(error)
	} catch (e: IOException) {
		Timber.e(e)
		error = ErrorIdentification.Unknown()
		Resource.error(ErrorIdentification.Unknown())
	} catch (e: JsonDataException) {
		Timber.e(e)
		error = ErrorIdentification.MessageError(message = R.string.error_invalid_data_format)
		Resource.error(error)
	} finally {
		error?.let {
			networkError.sendError(error = it)
		}
	}
}

fun convertUrlImageIntoBase64Image(
	originalUrlImage: String?,
	originalBase64Image: String?,
	bitmapCompressQuality: Int = 50,
	compressFormat: CompressFormat = CompressFormat.JPEG
): String? {
	var base64Image = originalBase64Image

	if (originalUrlImage != null && originalBase64Image == null) {
		try {
			val url = URL(originalUrlImage)
			val image: Bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
			val baos = ByteArrayOutputStream()
			image.compress(compressFormat, bitmapCompressQuality, baos)
			base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT or Base64.NO_WRAP)
		} catch (e: IOException) {
			Timber.e(e)
		}
	}
	return base64Image
}

private fun <E, O> mapResource(
	originalResource: Resource<E>,
	mapper: (E?) -> O?
): Resource<O> {
	return Resource(
		status = originalResource.status,
		data = mapper(originalResource.data),
		errorIdentification = originalResource.errorIdentification
	)
}