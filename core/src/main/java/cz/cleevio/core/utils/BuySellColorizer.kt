package cz.cleevio.core.utils

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

object BuySellColorizer {

	fun colorizeTransactionType(wholeText: String, userName: String, nameTv: TextView, @ColorRes color: Int) {
		val spannableStringBuilder = SpannableStringBuilder(wholeText)
		spannableStringBuilder.setSpan(
			ForegroundColorSpan(ContextCompat.getColor(nameTv.context, color)),
			userName.length,
			wholeText.length,
			Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
		)

		nameTv.text = spannableStringBuilder
	}
}
