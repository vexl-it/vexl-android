package cz.cleevio.network.language

import android.content.Context
import cz.cleevio.network.R

class LanguageTrackerImpl constructor(
	private val appContext: Context
) : LanguageTracker {

	override fun getAppLanguage(): String = appContext.getString(R.string.app_language_code)
}