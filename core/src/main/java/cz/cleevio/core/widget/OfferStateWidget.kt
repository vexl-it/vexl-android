package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferStateBinding
import lightbase.core.extensions.layoutInflater


class OfferStateWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferStateBinding

	private var onDelete: () -> Unit = {}
	private var onPause: () -> Unit = {}

	init {
		setupUI()

		binding.stateDelete.setOnClickListener {
			onDelete()
		}

		binding.statePause.setOnClickListener {
			onPause()
		}
	}

	private fun setupUI() {
		binding = WidgetOfferStateBinding.inflate(layoutInflater, this)
	}

	fun setActive(active: Boolean) {
		//todo: also update text color and icon color
		if (active) {
			binding.stateActive.text = context.getText(R.string.widget_offer_state_active)
		} else {
			binding.stateActive.text = context.getText(R.string.widget_offer_state_inactive)
		}
	}

	fun setListeners(onDelete: () -> Unit, onPause: () -> Unit) {
		this.onDelete = onDelete
		this.onPause = onPause
	}
}