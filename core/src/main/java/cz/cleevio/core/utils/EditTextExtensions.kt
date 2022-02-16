package cz.cleevio.core.utils

import android.content.Context
import android.text.method.DigitsKeyListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import java.text.DecimalFormatSymbols

fun EditText.setDecimalDigitKeyListener() {
	this.keyListener = DigitsKeyListener.getInstance("0123456789${DecimalFormatSymbols.getInstance().decimalSeparator}")
}

fun EditText.showKeyboard() {
	this.post {
		this.requestFocus()
		this.setSelection(this.text.length)
		val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
	}
}