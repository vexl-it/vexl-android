package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.databinding.WidgetOfferLocationItemBinding
import cz.cleevio.repository.model.offer.Location
import lightbase.core.extensions.layoutInflater
import java.math.BigDecimal

class OfferLocationItem @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferLocationItemBinding
	private var onCloseListener: ((OfferLocationItem) -> Unit)? = null

	init {
		setupUI()

		binding.locationItemClose.setOnClickListener {
			onCloseListener?.invoke(this)
		}
	}

	private fun setupUI() {
		binding = WidgetOfferLocationItemBinding.inflate(layoutInflater, this)
	}

	fun setOnCloseListener(listener: (OfferLocationItem) -> Unit) {
		onCloseListener = listener
	}

	fun getValue(): String = binding.locationItemText.text.toString()

	fun getRadius(): String = binding.locationItemRadius.text.toString()

	fun reset() {
		binding.locationItemText.setText("")
		binding.locationItemRadius.setText("+- 1 km")
	}

	@Deprecated("Use getValue and getRadius instead")
	fun getLocation(): Location = Location(
		latitude = BigDecimal("50.0811704"),
		longitude = BigDecimal("14.4084831"),
		radius = BigDecimal("20")
	)
}