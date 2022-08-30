package cz.cleevio.repository.model

import android.content.Context
import cz.cleevio.repository.R


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

		fun Currency.getCurrencySymbol(context: Context): String = when (this) {
			CZK -> context.resources.getString(R.string.general_czk_sign)
			USD -> context.resources.getString(R.string.general_usd_sign)
			EUR -> context.resources.getString(R.string.general_eur_sign)
		}
	}
}