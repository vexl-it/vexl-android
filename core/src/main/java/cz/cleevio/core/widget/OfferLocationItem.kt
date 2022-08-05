package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferLocationItemBinding
import cz.cleevio.repository.model.offer.Location
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
	private var fragmentManager: FragmentManager? = null

	var onFocusChangeListener: ((Boolean, Int) -> Unit)? = null
	var onTextChanged: ((String) -> Unit)? = null

	init {
		setupUI()

		binding.locationItemText.setOnFocusChangeListener { _, hasFocus ->
			onFocusChangeListener?.invoke(hasFocus, binding.locationItemText.y.toInt())
		}

		binding.locationItemClose.setOnClickListener {
			onCloseListener?.invoke(this)
		}

		binding.locationItemText.doAfterTextChanged {
			onTextChanged?.invoke(it.toString())
		}

		binding.locationItemRadius.setOnClickListener {
			fragmentManager?.let { manager ->
				val bottomSheetDialog = NumberPickerBottomSheetDialog()
				bottomSheetDialog.setInitialValue(radius)
				bottomSheetDialog.setOnDoneListener { result ->
					radius = result
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

	fun setOnCloseListener(listener: (OfferLocationItem) -> Unit) {
		onCloseListener = listener
	}

	fun getValue(): String = binding.locationItemText.text.toString()

	fun getRadius(): Int = radius

	fun reset() {
		radius = 1
		updateRadiusText(radius)
		binding.locationItemText.setText("")
	}

	@Deprecated("Use getValue and getRadius instead")
	fun getLocation(): Location = Location(
		latitude = BigDecimal("50.0811704"),
		longitude = BigDecimal("14.4084831"),
		radius = BigDecimal("20")
	)

	fun setFragmentManager(manager: FragmentManager) {
		fragmentManager = manager
	}

	fun setValue(location: Location) {
		radius = location.radius.toInt()
		updateRadiusText(radius)

		//todo: we need to translate lat/lon to City name here
		binding.locationItemText.setText("Placeholder: City")
	}
}
