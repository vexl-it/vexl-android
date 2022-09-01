package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import cz.cleevio.core.databinding.WidgetOfferFilterLocationBinding
import cz.cleevio.repository.model.offer.Location
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater

class OfferFilterLocationWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferFilterLocationBinding
	private var selectedButtons: MutableSet<LocationButtonSelected> = mutableSetOf()

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

		binding.locationOnline.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				selectedButtons.add(LocationButtonSelected.ONLINE)
			} else {
				selectedButtons.remove(LocationButtonSelected.ONLINE)
			}
		}

		binding.locationInPerson.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				selectedButtons.add(LocationButtonSelected.IN_PERSON)
			} else {
				selectedButtons.remove(LocationButtonSelected.IN_PERSON)
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
		binding = WidgetOfferFilterLocationBinding.inflate(layoutInflater, this)
	}

	fun getLocationValues() = visibleItems.mapNotNull { it.getLocation() }

	fun getSelectedLocationTypes() = selectedButtons.toSet()

	fun reset() {
		items.forEach {
			it.reset()
			it.isVisible = false
		}

		visibleItems.clear()

		binding.locationOnline.isChecked = false
		binding.locationInPerson.isChecked = false
		selectedButtons = mutableSetOf()
	}

	fun setFragmentManager(fragmentManager: FragmentManager) {
		items.forEach {
			it.setFragmentManager(fragmentManager)
		}
	}

	fun setValues(location: List<Location>, selected: List<LocationButtonSelected>) {
		location.forEachIndexed { index, location ->
			val item = items[index]
			item.setValue(location)
			item.isVisible = true
			visibleItems.add(item)
		}
		checkAddButtonVisibility()

		selectedButtons = selected.toMutableSet()
		updateSelectedButton()
	}

	private fun updateSelectedButton() {
		binding.locationInPerson.isChecked = selectedButtons.contains(LocationButtonSelected.IN_PERSON)
		binding.locationOnline.isChecked = selectedButtons.contains(LocationButtonSelected.ONLINE)
	}

	companion object {
		const val LOCATION_ITEMS_LIMIT = 5
	}
}
