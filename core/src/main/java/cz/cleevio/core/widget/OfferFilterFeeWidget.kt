package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferFilterFeeBinding
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater

class OfferFilterFeeWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferFilterFeeBinding
	private var selectedButtons: MutableSet<FeeButtonSelected> = mutableSetOf()
	private var feeValue: Float = FEE_MIN_VALUE

	init {
		setupUI()

		binding.feeOk.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				selectedButtons.add(FeeButtonSelected.WITH_FEE)
				binding.feeBar.value = 1f
				updateFeeValue(1f)
			} else {
				selectedButtons.remove(FeeButtonSelected.WITH_FEE)
			}
			drawSeekbarSection()
		}

		binding.feeWithout.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				selectedButtons.add(FeeButtonSelected.WITHOUT_FEE)
			} else {
				selectedButtons.remove(FeeButtonSelected.WITHOUT_FEE)
			}
		}

		binding.feeBar.value = FEE_MIN_VALUE
		binding.feeBar.valueFrom = FEE_MIN_VALUE
		binding.feeBar.valueTo = FEE_MAX_VALUE
		binding.feeBar.addOnChangeListener { _, value, _ ->
			updateFeeValue(value)
		}

		//init values
		updateFeeValue(FEE_MIN_VALUE)
		drawSeekbarSection()
	}

	private fun setupUI() {
		binding = WidgetOfferFilterFeeBinding.inflate(layoutInflater, this)

		binding.feeBar.setCustomThumbDrawable(R.drawable.ic_picker)
	}

	private fun drawSeekbarSection() {
		val visible = FeeButtonSelected.WITH_FEE in selectedButtons
		binding.feeValue.isVisible = visible
		binding.feeBar.isVisible = visible
	}

	private fun updateFeeValue(value: Float) {
		feeValue = value
		binding.feeValue.text = context.getString(R.string.widget_fee_percent, feeValue.toInt().toString())
	}

	fun getFeeValue() = feeValue

	fun getFeeTypes() = selectedButtons.toSet()

	fun reset() {
		binding.feeOk.isChecked = false
		binding.feeWithout.isChecked = false
		selectedButtons = mutableSetOf()
		drawSeekbarSection()
		updateFeeValue(FEE_MIN_VALUE)
	}

	fun setValues(value: Float?, selected: List<FeeButtonSelected>) {
		selectedButtons = selected.toMutableSet()
		updateSelectedButton()
		value?.let {
			updateFeeValue(value)
			binding.feeBar.value = value
			drawSeekbarSection()
		}
	}

	private fun updateSelectedButton() {
		binding.feeWithout.isChecked = selectedButtons.contains(FeeButtonSelected.WITHOUT_FEE)
		binding.feeOk.isChecked = selectedButtons.contains(FeeButtonSelected.WITH_FEE)
	}

	companion object {
		const val FEE_MIN_VALUE = 1f
		const val FEE_MAX_VALUE = 10f
	}
}
