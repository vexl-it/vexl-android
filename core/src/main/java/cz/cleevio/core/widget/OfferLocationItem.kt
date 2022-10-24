package cz.cleevio.core.widget

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import cz.cleevio.core.databinding.WidgetOfferLocationItemBinding
import cz.cleevio.repository.model.offer.Location
import cz.cleevio.repository.model.offer.LocationSuggestion
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater

class OfferLocationItem @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferLocationItemBinding
	private var onCloseListener: ((OfferLocationItem) -> Unit)? = null

	private var location: Location? = null
	private var fragmentManager: FragmentManager? = null

	var onFocusChangeListener: ((Boolean, OfferLocationItem) -> Unit)? = null
	var onTextChanged: ((String, OfferLocationItem) -> Unit)? = null

	init {
		setupUI()

		binding.locationItemText.imeOptions = EditorInfo.IME_ACTION_DONE
		binding.locationItemText.setRawInputType(InputType.TYPE_CLASS_TEXT)
		binding.locationItemText.setOnFocusChangeListener { _, hasFocus ->
			onFocusChangeListener?.invoke(hasFocus, this)
		}

		binding.locationItemClose.setOnClickListener {
			onCloseListener?.invoke(this)
		}

		binding.locationItemText.doAfterTextChanged {
			onTextChanged?.invoke(it.toString(), this)
		}
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

	fun reset() {
		binding.locationItemText.setText("")
	}

	fun getLocation(): Location? = location

	fun setFragmentManager(manager: FragmentManager) {
		fragmentManager = manager
	}

	fun setValue(location: Location) {
		this.location = location
		binding.locationItemText.setText(location.city)
	}

	fun setLocation(suggestion: LocationSuggestion) {
		this.location = Location(
			longitude = suggestion.longitude,
			latitude = suggestion.latitude,
			city = suggestion.cityText
		)
	}
}
