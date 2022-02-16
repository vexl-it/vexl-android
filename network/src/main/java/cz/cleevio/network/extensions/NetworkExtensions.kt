package cz.cleevio.network.extensions

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import cz.cleevio.network.R
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.response.BaseResponseJsonAdapter
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend fun <E> tryOnline(
	doOnSuccess: suspend ((E?) -> Unit) = {},
	doOnError: suspend ((Int, Int?) -> Unit) = { _, _ -> },
	parseError: suspend ((Response<E>) -> Resource<E>) = { response ->
		parseResponseToResource(response)
	},
	request: suspend () -> Response<E>
): Resource<E> {
	return try {
		val response = request()
		if (response.isSuccessful) {
			doOnSuccess(response.body())
			Resource.success(response.body())
		} else {
			val resource = parseError(response)
			doOnError(response.code(), resource.errorIdentification.subcode)
			resource
		}
	} catch (e: HttpException) {
		Timber.e(e)
		doOnError(e.code(), null)
		Resource.errorUnknown(code = e.code())
	} catch (e: SocketTimeoutException) {
		Timber.e(e)
		Resource.error(ErrorIdentification.ConnectionProblem)
	} catch (e: UnknownHostException) {
		Timber.e(e)
		Resource.error(ErrorIdentification.ConnectionProblem)
	} catch (e: IOException) {
		Timber.e(e)
		Resource.error(ErrorIdentification.Unknown())
	} catch (e: JsonDataException) {
		Timber.e(e)
		Resource.error(ErrorIdentification.MessageError(message = R.string.error_invalid_data_format))
	}
}

fun <T> parseResponseToResource(response: Response<T>): Resource<T> {
	return if (response.isSuccessful) {
		Resource.success(response.body())
	} else {
		val subcode = response.errorBody()
			?.source()
			?.let {
				BaseResponseJsonAdapter(
					moshi = Moshi.Builder().build()
				).fromJson(it)
					?.code
					?.toIntOrNull()
			}
		Resource.error(code = response.code(), subcode = subcode)
	}
}