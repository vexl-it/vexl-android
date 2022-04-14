package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.FrameLayout
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferLocationBinding
import lightbase.core.extensions.layoutInflater


class OfferLocationWidget constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

	private lateinit var binding: WidgetOfferLocationBinding
	private var selectedButton: LocationButtonSelected = LocationButtonSelected.NONE

	init {
		setupUI()

		binding.locationOnline.setOnClickListener {
			selectedButton = LocationButtonSelected.ONLINE
			redrawWidget()
		}

		binding.locationInPerson.setOnClickListener {
			selectedButton = LocationButtonSelected.IN_PERSON
			redrawWidget()
		}

		//init
		redrawWidget()
	}

	private fun setupUI() {
		binding = WidgetOfferLocationBinding.inflate(layoutInflater, this)
	}

	private fun redrawWidget() {
		when (selectedButton) {
			LocationButtonSelected.NONE -> {
				drawButton(binding.locationOnline)
				drawButton(binding.locationInPerson)
			}
			LocationButtonSelected.ONLINE -> {
				drawButton(binding.locationOnline, true)
				drawButton(binding.locationInPerson)
			}
			LocationButtonSelected.IN_PERSON -> {
				drawButton(binding.locationOnline)
				drawButton(binding.locationInPerson, true)
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
}

enum class LocationButtonSelected {
	NONE, ONLINE, IN_PERSON
}