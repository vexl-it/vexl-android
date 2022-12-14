package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.databinding.WidgetOfferBtcNetworkWidgetBinding
import cz.cleevio.core.model.BtcNetworkValue
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater

class OfferBtcNetworkType @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferBtcNetworkWidgetBinding
	private var selectedButtons: MutableSet<BtcNetworkButtonSelected> = mutableSetOf()

	init {
		setupUI()

		binding.btcNetworkLighting.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				selectedButtons.add(BtcNetworkButtonSelected.LIGHTING)
			} else {
				selectedButtons.remove(BtcNetworkButtonSelected.LIGHTING)
			}
		}

		binding.btcNetworkOnChain.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				selectedButtons.add(BtcNetworkButtonSelected.ON_CHAIN)
			} else {
				selectedButtons.remove(BtcNetworkButtonSelected.ON_CHAIN)
			}
		}
	}

	private fun setupUI() {
		binding = WidgetOfferBtcNetworkWidgetBinding.inflate(layoutInflater, this)
	}

	fun getBtcNetworkValue(): BtcNetworkValue = BtcNetworkValue(
		value = selectedButtons.toList()
	)

	fun reset() {
		binding.btcNetworkLighting.isChecked = false
		binding.btcNetworkOnChain.isChecked = false

		selectedButtons = mutableSetOf()
	}

	fun setValues(buttons: List<BtcNetworkButtonSelected>) {
		selectedButtons = buttons.toMutableSet()
		updateButtonView(selectedButtons)
	}

	private fun updateButtonView(selectedButtons: MutableSet<BtcNetworkButtonSelected>) {
		binding.btcNetworkLighting.isChecked = selectedButtons.contains(BtcNetworkButtonSelected.LIGHTING)
		binding.btcNetworkOnChain.isChecked = selectedButtons.contains(BtcNetworkButtonSelected.ON_CHAIN)
	}
}

enum class BtcNetworkButtonSelected {
	NONE, LIGHTING, ON_CHAIN
}