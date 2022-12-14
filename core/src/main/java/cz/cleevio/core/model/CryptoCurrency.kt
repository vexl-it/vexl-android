package cz.cleevio.core.model

enum class CryptoCurrency {
	BITCOIN;

	companion object {
		fun CryptoCurrency.getExactName(): String {
			return when (this) {
				BITCOIN -> "bitcoin"
			}
		}

		fun String.mapStringToCryptoCurrency(): CryptoCurrency {
			return if (this == "bitcoin") {
				BITCOIN
			} else {
				BITCOIN
			}
		}
	}
}