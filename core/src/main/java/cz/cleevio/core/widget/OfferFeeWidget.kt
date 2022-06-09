package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.core.view.isVisible
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferFeeBinding
import cz.cleevio.core.model.FeeValue
import lightbase.core.extensions.layoutInflater
import timber.log.Timber

class OfferFeeWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferFeeBinding
	private var selectedButton: FeeButtonSelected = FeeButtonSelected.NONE
	private var feeValue: Int = 0

	init {
		setupUI()

		binding.feeRadiogroup.setOnCheckedChangeListener { _, id ->
			selectedButton = when (id) {
				R.id.fee_without -> {
					FeeButtonSelected.WITHOUT_FEE
				}
				R.id.fee_ok -> {
					FeeButtonSelected.WITH_FEE
				}
				else -> {
					Timber.e("Unknown radio ID! '$id'")

					FeeButtonSelected.NONE
				}
			}
			drawSeekbarSection(selectedButton)
		}

		binding.feeBar.max = FEE_MAX_VALUE
		binding.feeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				updateFeeValue(progress)
			}

			override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
			override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
		})

		//init values
		updateFeeValue(0)
		drawSeekbarSection(selectedButton)
	}

	private fun setupUI() {
		binding = WidgetOfferFeeBinding.inflate(layoutInflater, this)
	}

	private fun drawSeekbarSection(selectedButton: FeeButtonSelected) {
		val visible = (selectedButton == FeeButtonSelected.WITH_FEE)
		binding.feeDivider.isVisible = visible
		binding.feeValue.isVisible = visible
		binding.feeBar.isVisible = visible
	}

	private fun updateFeeValue(value: Int) {
		feeValue = value.inc()
		binding.feeValue.text = context.getString(R.string.widget_fee_percent, feeValue.toString())
	}

	fun getFeeValue(): FeeValue = FeeValue(
		type = selectedButton,
		value = feeValue
	)

	fun reset() {
		selectedButton = FeeButtonSelected.WITHOUT_FEE
		drawSeekbarSection(selectedButton)
		updateFeeValue(0)
	}

	fun setValues(data: FeeValue) {
		selectedButton = data.type
		drawSeekbarSection(selectedButton)
		updateFeeValue(data.value)
	}

	companion object {
		const val FEE_MAX_VALUE: Int = 24
	}
}

enum class FeeButtonSelected {
	NONE, WITHOUT_FEE, WITH_FEE
}