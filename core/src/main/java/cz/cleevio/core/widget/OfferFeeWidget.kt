package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.core.view.isVisible
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferFeeBinding
import lightbase.core.extensions.layoutInflater

class OfferFeeWidget constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

	private lateinit var binding: WidgetOfferFeeBinding
	private var selectedButton: FeeButtonSelected = FeeButtonSelected.NONE
	private var feeValue: Int = 0

	init {
		setupUI()

		binding.feeOk.setOnClickListener {
			selectedButton = FeeButtonSelected.WITH_FEE
			redrawWidget()
		}

		binding.feeWithout.setOnClickListener {
			selectedButton = FeeButtonSelected.WITHOUT_FEE
			redrawWidget()
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
		redrawWidget()
	}

	private fun setupUI() {
		binding = WidgetOfferFeeBinding.inflate(layoutInflater, this)
	}

	private fun redrawWidget() {
		when (selectedButton) {
			FeeButtonSelected.NONE -> {
				//draw both button grey
				drawButton(binding.feeWithout)
				drawButton(binding.feeOk)
				//hide seekbar section
				drawSeekbarSection()
			}
			FeeButtonSelected.WITHOUT_FEE -> {
				//draw first button green with active background
				drawButton(binding.feeWithout, true)
				//draw second button grey
				drawButton(binding.feeOk)
				//hide seekbar section
				drawSeekbarSection()
			}
			FeeButtonSelected.WITH_FEE -> {
				//draw first button grey
				drawButton(binding.feeWithout)
				//draw second button green with active background
				drawButton(binding.feeOk, true)
				//show seekbar section
				drawSeekbarSection(true)
			}
		}
	}

	private fun drawButton(button: Button, isSelected: Boolean = false) {
		if (isSelected) {
			//TODO: set background

			//set green_5 text
			button.setTextColor(context.getColor(R.color.green_5))
		} else {
			//TODO: remove background

			//set grey text
			button.setTextColor(context.getColor(R.color.gray))
		}
	}

	private fun drawSeekbarSection(visible: Boolean = false) {
		binding.feeDivider.isVisible = visible
		binding.feeValue.isVisible = visible
		binding.feeBar.isVisible = visible
	}

	private fun updateFeeValue(value: Int) {
		feeValue = value.inc()
		binding.feeValue.text = context.getString(R.string.widget_fee_percent, feeValue.toString())
	}

	fun setSelectedButton(newValue: FeeButtonSelected) {
		selectedButton = newValue
		redrawWidget()
	}

	companion object {
		const val FEE_MAX_VALUE: Int = 24
	}
}

enum class FeeButtonSelected {
	NONE, WITHOUT_FEE, WITH_FEE
}