package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferLocationBinding
import cz.cleevio.core.model.LocationValue
import cz.cleevio.repository.model.offer.Location
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater
import timber.log.Timber

class OfferLocationWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferLocationBinding
	private var selectedButton: LocationButtonSelected = LocationButtonSelected.ONLINE

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
			val firstGoneItem = items.first { !it.isVisible }
			firstGoneItem.isVisible = true
			visibleItems.add(firstGoneItem)

			checkAddButtonVisibility()
		}

		items.forEach { view ->
			view.setOnCloseListener { item ->
				val clickedItem = items.firstOrNull { it == item }
				clickedItem?.let {
					it.isVisible = false
					it.reset()
					visibleItems.remove(it)
				}
				checkAddButtonVisibility()
			}
		}
	}

	fun setupFocusChangeListener(onFocusChangeListener: (Boolean, OfferLocationItem) -> Unit) {
		items.forEach {
			it.onFocusChangeListener = onFocusChangeListener
		}
	}

	fun setupOnTextChanged(onTextChanged: (String, OfferLocationItem) -> Unit) {
		items.forEach {
			it.onTextChanged = onTextChanged
		}
	}

	fun getPositionOfItem(offerLocationItem: OfferLocationItem) =
		items.indexOf(offerLocationItem)

	private fun checkAddButtonVisibility() {
		binding.locationAddNewLocation.isVisible = visibleItems.size < LOCATION_ITEMS_LIMIT
	}

	private fun setupUI() {
		binding = WidgetOfferLocationBinding.inflate(layoutInflater, this)
	}

	fun getLocationValue(): LocationValue = LocationValue(
		type = selectedButton,
		values = visibleItems.mapNotNull { it.getLocation() }
	)

	fun reset() {
		items.forEach {
			it.reset()
			it.isVisible = false
		}

		visibleItems.clear()

		selectedButton = LocationButtonSelected.NONE
		updateSelectedButton(selectedButton)
	}

	fun setFragmentManager(fragmentManager: FragmentManager) {
		items.forEach {
			it.setFragmentManager(fragmentManager)
		}
	}

	fun setValues(location: List<Location>, locationButton: LocationButtonSelected) {
		location.forEachIndexed { index, location ->
			val item = items[index]
			item.setValue(location)
			item.isVisible = true
			visibleItems.add(item)
		}
		checkAddButtonVisibility()

		selectedButton = locationButton
		updateSelectedButton(selectedButton)
	}

	private fun updateSelectedButton(selectedButton: LocationButtonSelected) {
		when (selectedButton) {
			LocationButtonSelected.NONE -> {
				binding.locationInPerson.isChecked = false
				binding.locationOnline.isChecked = false
				binding.locationRadiogroup.clearCheck()
			}
			LocationButtonSelected.ONLINE -> {
				binding.locationInPerson.isChecked = false
				binding.locationOnline.isChecked = true
			}
			LocationButtonSelected.IN_PERSON -> {
				binding.locationInPerson.isChecked = true
				binding.locationOnline.isChecked = false
			}
		}
	}

	companion object {
		const val LOCATION_ITEMS_LIMIT = 5
	}
}

enum class LocationButtonSelected {
	NONE, ONLINE, IN_PERSON
}
