package cz.cleevio.core.model

enum class Currency {
	CZK,
	USD,
	EUR;

	companion object {
		fun String.mapStringToCurrency(): Currency {
			return when (this) {
				CZK.name -> CZK
				EUR.name -> EUR
				else -> USD
			}
		}
	}
}