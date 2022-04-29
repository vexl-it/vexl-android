package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.google.android.material.textfield.TextInputEditText
import cz.cleevio.core.databinding.WidgetOfferLocationItemBinding
import cz.cleevio.repository.model.offer.Location
import lightbase.core.extensions.layoutInflater

class OfferLocationItem @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferLocationItemBinding

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetOfferLocationItemBinding.inflate(layoutInflater, this)
	}

	private fun getFloat(view: TextInputEditText): Float {
		return try {
			view.text.toString().toFloat()
		} catch (exception: Exception) {
			0.0f
		}
	}

	fun getLocation(): Location = Location(
		latitude = getFloat(binding.locationItemLatitude),
		longitude = getFloat(binding.locationItemLongitude),
		radius = getFloat(binding.locationItemRadius)
	)
}