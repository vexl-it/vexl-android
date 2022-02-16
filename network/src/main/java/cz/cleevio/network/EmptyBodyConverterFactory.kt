package cz.cleevio.network

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

object EmptyBodyConverterFactory : Converter.Factory() {
	fun converterFactory() = this
	override fun responseBodyConverter(
		type: Type,
		annotations: Array<out Annotation>,
		retrofit: Retrofit
	) = object : Converter<ResponseBody, Any?> {
		val nextResponseBodyConverter =
			retrofit.nextResponseBodyConverter<Any?>(
				converterFactory(),
				type,
				annotations
			)

		override fun convert(value: ResponseBody): Any? {
			return if (value.contentLength() != 0L) {
				nextResponseBodyConverter.convert(value)
			} else {
				null
			}
		}
	}
}