package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferFeeBinding
import cz.cleevio.core.model.FeeValue
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater
import timber.log.Timber

class OfferFeeWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferFeeBinding
	private var selectedButton: FeeButtonSelected = FeeButtonSelected.WITHOUT_FEE
	private var feeValue: Float = FEE_MIN_VALUE

	init {
		setupUI()

		binding.feeRadiogroup.setOnCheckedChangeListener { _, id ->
			when (id) {
				R.id.fee_without -> {
					selectedButton = FeeButtonSelected.WITHOUT_FEE
					setValues(FeeValue(FeeButtonSelected.WITHOUT_FEE, FEE_WITHOUT_VALUE))
					binding.feeBar.value = FEE_WITHOUT_VALUE
				}
				R.id.fee_ok -> {
					selectedButton = FeeButtonSelected.WITH_FEE
					setValues(FeeValue(FeeButtonSelected.WITH_FEE, FEE_OK_VALUE))
					binding.feeBar.value = FEE_OK_VALUE
				}
				else -> {
					Timber.e("Unknown radio ID! '$id'")
					selectedButton = FeeButtonSelected.NONE
				}
			}
			drawSeekbarSection(selectedButton)
		}

		binding.feeBar.value = FEE_MIN_VALUE
		binding.feeBar.valueFrom = FEE_MIN_VALUE
		binding.feeBar.valueTo = FEE_MAX_VALUE
		binding.feeBar.addOnChangeListener { _, value, _ ->
			updateFeeValue(value)
		}

		//init values
		updateFeeValue(FEE_MIN_VALUE)
		drawSeekbarSection(selectedButton)
	}

	private fun setupUI() {
		binding = WidgetOfferFeeBinding.inflate(layoutInflater, this)

		binding.feeBar.setCustomThumbDrawable(R.drawable.ic_picker)
	}

	private fun drawSeekbarSection(selectedButton: FeeButtonSelected) {
		val visible = selectedButton == FeeButtonSelected.WITH_FEE
		binding.feeValue.isVisible = visible
		binding.feeBar.isVisible = visible
	}

	private fun updateFeeValue(value: Float) {
		feeValue = value
		binding.feeValue.text = context.getString(R.string.widget_fee_percent, feeValue.toInt().toString())
	}

	fun getFeeValue(): FeeValue = FeeValue(
		type = selectedButton,
		value = feeValue
	)

	fun reset() {
		selectedButton = FeeButtonSelected.WITHOUT_FEE
		drawSeekbarSection(selectedButton)
		binding.feeRadiogroup.check(R.id.fee_without)
		updateFeeValue(FEE_MIN_VALUE)
	}

	fun setValues(data: FeeValue) {
		selectedButton = data.type
		if (selectedButton == FeeButtonSelected.WITH_FEE) {
			binding.feeRadiogroup.check(R.id.fee_ok)
		} else {
			binding.feeRadiogroup.check(R.id.fee_without)
		}
		drawSeekbarSection(selectedButton)
		updateFeeValue(data.value)
		binding.feeBar.value = data.value
	}

	companion object {
		const val FEE_OK_VALUE = 1f
		const val FEE_WITHOUT_VALUE = 0f
		const val FEE_MIN_VALUE = 1f
		const val FEE_MAX_VALUE = 10f
	}
}

enum class FeeButtonSelected {
	NONE, WITHOUT_FEE, WITH_FEE
}
