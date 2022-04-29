package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferLocationBinding
import cz.cleevio.core.model.LocationValue
import lightbase.core.extensions.layoutInflater
import timber.log.Timber

class OfferLocationWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferLocationBinding
	private var selectedButton: LocationButtonSelected = LocationButtonSelected.NONE

	private val visibleItems = mutableListOf<OfferLocationItem>()
	private val items: List<OfferLocationItem>

	init {
		setupUI()

		items = listOf(
			binding.locationNewOne,
			binding.locationNewTwo,
			binding.locationNewThree,
			binding.locationNewFour,
			binding.locationNewFive
		)

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

		binding.locationAddNewLocation.setOnClickListener {
			items[visibleItems.size].isVisible = true
			visibleItems.add(items[visibleItems.size])

			if (visibleItems.size >= LOCATION_ITEMS_LIMIT) {
				binding.locationAddNewLocation.isVisible = false
			}
		}
	}

	private fun setupUI() {
		binding = WidgetOfferLocationBinding.inflate(layoutInflater, this)
	}

	fun getLocationValue(): LocationValue = LocationValue(
		type = selectedButton,
		values = visibleItems.map { it.getLocation() }
	)

	companion object {
		const val LOCATION_ITEMS_LIMIT = 5
	}
}

enum class LocationButtonSelected {
	NONE, ONLINE, IN_PERSON
}