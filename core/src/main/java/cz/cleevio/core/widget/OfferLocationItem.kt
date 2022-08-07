package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferLocationItemBinding
import cz.cleevio.repository.model.offer.Location
import cz.cleevio.repository.model.offer.LocationSuggestion
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater
import java.math.BigDecimal

class OfferLocationItem @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferLocationItemBinding
	private var onCloseListener: ((OfferLocationItem) -> Unit)? = null

	private var radius: Int = 1
	private var location: Location? = null
	private var fragmentManager: FragmentManager? = null

	var onFocusChangeListener: ((Boolean, OfferLocationItem) -> Unit)? = null
	var onTextChanged: ((String, OfferLocationItem) -> Unit)? = null

	init {
		setupUI()

		binding.locationItemText.setOnFocusChangeListener { _, hasFocus ->
			onFocusChangeListener?.invoke(hasFocus, this)
		}

		binding.locationItemClose.setOnClickListener {
			onCloseListener?.invoke(this)
		}

		binding.locationItemText.doAfterTextChanged {
			onTextChanged?.invoke(it.toString(), this)
		}

		binding.locationItemRadius.setOnClickListener {
			fragmentManager?.let { manager ->
				val bottomSheetDialog = NumberPickerBottomSheetDialog()
				bottomSheetDialog.setInitialValue(radius)
				bottomSheetDialog.setOnDoneListener { result ->
					radius = result
					this.location?.radius = BigDecimal(radius)
					updateRadiusText(radius)
					bottomSheetDialog.dismiss()
				}
				bottomSheetDialog.show(manager, "NumberPickerBottomSheetDialog")
			}
		}

		updateRadiusText(radius)
	}

	private fun updateRadiusText(radius: Int) {
		binding.locationItemRadius.text = context.getString(R.string.widget_location_km, radius)
	}

	private fun setupUI() {
		binding = WidgetOfferLocationItemBinding.inflate(layoutInflater, this)
	}

	fun getEditText(): AutoCompleteTextView =
		binding.locationItemText

	fun setOnCloseListener(listener: (OfferLocationItem) -> Unit) {
		onCloseListener = listener
	}

	fun getValue(): String = binding.locationItemText.text.toString()

	private fun getRadius(): Int = radius

	fun reset() {
		radius = 1
		updateRadiusText(radius)
		binding.locationItemText.setText("")
	}

	fun getLocation(): Location? = location

	fun setFragmentManager(manager: FragmentManager) {
		fragmentManager = manager
	}

	fun setValue(location: Location) {
		radius = location.radius.toInt()
		this.location = location
		updateRadiusText(radius)
		binding.locationItemText.setText(location.city)
	}

	fun setLocation(suggestion: LocationSuggestion) {
		this.location = Location(
			longitude = suggestion.longitude,
			latitude = suggestion.latitude,
			radius = BigDecimal(getRadius()),
			suggestion.city
		)
	}
}
