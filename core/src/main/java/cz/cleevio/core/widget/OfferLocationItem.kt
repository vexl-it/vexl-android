package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.google.android.material.textfield.TextInputEditText
import cz.cleevio.core.databinding.WidgetOfferLocationItemBinding
import cz.cleevio.repository.model.offer.Location
import lightbase.core.extensions.layoutInflater
import timber.log.Timber
import java.math.BigDecimal

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

	private fun getBigDecimal(view: TextInputEditText): BigDecimal {
		return try {
			BigDecimal(view.text.toString())
		} catch (exception: NumberFormatException) {
			Timber.e(exception)
			BigDecimal(0.0)
		}
	}

	fun getLocation(): Location = Location(
		latitude = getBigDecimal(binding.locationItemLatitude),
		longitude = getBigDecimal(binding.locationItemLongitude),
		radius = getBigDecimal(binding.locationItemRadius)
	)
}