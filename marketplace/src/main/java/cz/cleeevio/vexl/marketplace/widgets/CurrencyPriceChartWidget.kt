package cz.cleeevio.vexl.marketplace.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleeevio.vexl.marketplace.databinding.WidgetCurrencyPriceChartBinding
import cz.cleevio.core.utils.showOrGone
import cz.cleevio.repository.model.marketplace.CryptoCurrencies
import lightbase.core.extensions.layoutInflater
import org.koin.core.component.KoinComponent

class CurrencyPriceChartWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

	private lateinit var binding: WidgetCurrencyPriceChartBinding
	private var packed = true

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetCurrencyPriceChartBinding.inflate(layoutInflater, this)

		binding.wrapper.setOnClickListener {
			packed = !packed
			packView(packed)
		}
	}

	fun setupData(currentCryptoCurrencyPrice: CryptoCurrencies) {
		binding.currentPrice.text = currentCryptoCurrencyPrice.priceUsd.toString()
	}

	private fun packView(packed: Boolean) {
		binding.packedGroup.showOrGone(packed)
		binding.unpackedGroup.showOrGone(!packed)
	}

}