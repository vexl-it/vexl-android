package cz.cleevio.network.data

import cz.cleevio.network.R

sealed class ErrorIdentification constructor(
	val code: Int = CODE_UNKNOWN,
	val subcode: Int? = null,
	val message: Int? = null
) {
	object None : ErrorIdentification()
	object Unauthorized : ErrorIdentification(code = CODE_UNAUTHORIZED, message = R.string.error_unauthorized)
	object ConnectionProblem : ErrorIdentification(message = R.string.error_unknown_connection_problem)
	class Unknown constructor(
		code: Int? = null,
		subcode: Int? = null
	) : ErrorIdentification(
		code ?: CODE_UNKNOWN,
		subcode,
		R.string.error_unknown_error_occurred
	)

	class MessageError constructor(
		code: Int = CODE_UNKNOWN,
		subcode: Int? = null,
		message: Int? = R.string.error_unknown_error_occurred
	) : ErrorIdentification(
		code,
		subcode,
		message
	)

	override fun toString(): String =
		"${this.javaClass.simpleName}: $message}"

	companion object {
		private const val CODE_UNKNOWN = -1
		const val CODE_SUCCESS = 200
		const val CODE_SUCCESS_CREATED = 201
		const val CODE_WRONG_ACCESS_TOKEN = 300
		const val CODE_VALIDATION_ERROR_400 = 400
		const val CODE_UNAUTHORIZED = 401
		const val CODE_FORBIDDEN_403 = 403
		const val CODE_ENTITY_NOT_EXIST_404 = 404
		const val CODE_METHOD_NOT_ALLOWED = 405
		const val CODE_CONFLICT_409 = 409
		const val CODE_INTERNAL_SERVER_ERROR = 500
		const val CODE_NOT_IMPLEMENTED = 501
		const val CODE_BAD_GATEWAY = 502

		const val SUBCODE_ZERO = 0
		const val SUBCODE_USER_IS_BLOCKED = 102
		const val SUBCODE_INVALID_PIN_SIZE = 104
		const val SUBCODE_USER_ALREADY_EXISTS = 101_101
		const val SUBCODE_USER_DOES_NOT_EXIST = 100_100
		const val SUBCODE_USER_IS_NOT_CONFIRMED = 101_505
		const val SUBCODE_AVATAR_HAS_INVALID_FORMAT = 103_203

		fun parseErrorCodes(code: Int, subcode: Int?): ErrorIdentification {
			return when {
				code == CODE_SUCCESS || code == CODE_SUCCESS_CREATED -> None
				code == CODE_UNAUTHORIZED -> Unauthorized
				code == CODE_FORBIDDEN_403 && subcode == SUBCODE_USER_IS_BLOCKED -> MessageError(
					code = code,
					subcode = subcode,
					message = R.string.error_user_is_blocked
				)

				// TODO parse more errors
				else -> Unknown(code, subcode)
			}
		}
	}
}