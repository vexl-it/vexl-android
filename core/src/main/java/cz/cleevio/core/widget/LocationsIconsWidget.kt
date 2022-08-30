package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetLocationsIconsBinding
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater

class LocationsIconsWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetLocationsIconsBinding

	init {
		setupUI()
	}

	fun bind(values: List<String>) {
		binding.oneLocationGroup.isVisible = false
		binding.twoLocationsGroup.isVisible = false

		@Suppress("MagicNumber")
		when (values.size) {
			1 -> {
				binding.oneLocationFirst.setImageResource(getIcon(values[0]))
				binding.oneLocationGroup.isVisible = true
			}
			2 -> {
				binding.twoLocationsFirst.setImageResource(getIcon(values[0]))
				binding.twoLocationsSecond.setImageResource(getIcon(values[1]))
				binding.twoLocationsGroup.isVisible = true
			}
			else -> {}
		}
	}

	private fun getIcon(locationValue: String): Int {
		return when (locationValue) {
			"ONLINE" -> R.drawable.ic_online
			"IN_PERSON" -> R.drawable.ic_map_pin
			else -> R.drawable.ic_map_pin
		}
	}

	private fun setupUI() {
		binding = WidgetLocationsIconsBinding.inflate(layoutInflater, this)
	}
}
