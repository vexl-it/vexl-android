package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferLocationBinding
import cz.cleevio.core.model.LocationValue
import lightbase.core.extensions.layoutInflater
import timber.log.Timber

class OfferLocationWidget constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferLocationBinding
	private var selectedButton: LocationButtonSelected = LocationButtonSelected.NONE

	init {
		setupUI()

		//todo: handle text color
		binding.locationRadiogroup.setOnCheckedChangeListener { _, id ->
			selectedButton = when (id) {
				R.id.location_in_person -> LocationButtonSelected.IN_PERSON
				R.id.location_online -> LocationButtonSelected.ONLINE
				else -> {
					Timber.e("Unknown radio ID! '$id'")
					LocationButtonSelected.NONE
				}
			}
		}
	}

	private fun setupUI() {
		binding = WidgetOfferLocationBinding.inflate(layoutInflater, this)
	}

	fun getLocationValue(): LocationValue = LocationValue(
		type = selectedButton
	)
}

enum class LocationButtonSelected {
	NONE, ONLINE, IN_PERSON
}