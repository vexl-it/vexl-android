package cz.cleevio.network.data

/**
 * A generic class that holds a value with its status.
 *
 * @param status
 * @param data
 * @param errorIdentification represents type of error
 */
data class Resource<T> constructor(
	val status: Status,
	val data: T? = null,
	val errorIdentification: ErrorIdentification = ErrorIdentification.None
) {

	fun isLoading(): Boolean = status is Status.Loading

	fun isSuccess(): Boolean = status is Status.Success

	fun isError(): Boolean = status is Status.Error

	fun isNotStarted(): Boolean = status is Status.NotStarted

	companion object {

		fun <T> notStarted(data: T? = null): Resource<T> =
			Resource(
				status = Status.NotStarted,
				data = data
			)

		fun <T> success(data: T? = null): Resource<T> =
			Resource(
				status = Status.Success,
				data = data
			)

		fun <T> loading(data: T? = null): Resource<T> =
			Resource(
				status = Status.Loading,
				data = data
			)

		fun <T> error(data: T? = null, code: Int, subcode: Int? = null): Resource<T> =
			Resource(
				status = Status.Error,
				data = data,
				errorIdentification = ErrorIdentification.parseErrorCodes(code, subcode)
			)

		fun <T> error(errorIdentification: ErrorIdentification, data: T? = null): Resource<T> =
			Resource(
				Status.Error,
				data = data,
				errorIdentification = errorIdentification
			)

		fun <T> errorUnknown(data: T? = null, code: Int, subcode: Int? = null): Resource<T> =
			Resource(
				Status.Error,
				data = data,
				errorIdentification = ErrorIdentification.Unknown(code, subcode)
			)
	}
}