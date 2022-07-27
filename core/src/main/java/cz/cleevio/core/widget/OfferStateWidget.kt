package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat.getColor
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferStateBinding
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater

class OfferStateWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferStateBinding

	private var onDelete: () -> Unit = {}
	private var onChangeActiveState: () -> Unit = {}

	init {
		setupUI()

		binding.stateDelete.setOnClickListener {
			onDelete()
		}

		binding.statePause.setOnClickListener {
			onChangeActiveState()
		}
	}

	private fun setupUI() {
		binding = WidgetOfferStateBinding.inflate(layoutInflater, this)
	}

	fun setActive(active: Boolean) {
		if (active) {
			binding.stateActive.text = context.getText(R.string.widget_offer_state_active)
			binding.stateActive.setTextColor(getColor(context, R.color.green_100))
			binding.stateActive.setCompoundDrawablesWithIntrinsicBounds(
				getDrawable(context, R.drawable.ic_ellipse_green), null, null, null
			)
			binding.statePauseText.text = context.getText(R.string.widget_offer_state_pause)
		} else {
			binding.stateActive.text = context.getText(R.string.widget_offer_state_inactive)
			binding.stateActive.setTextColor(getColor(context, R.color.gray_4))
			binding.stateActive.setCompoundDrawablesWithIntrinsicBounds(
				getDrawable(context, R.drawable.ic_ellipse_gray), null, null, null
			)
			binding.statePauseText.text = context.getText(R.string.widget_offer_state_activate)
		}
	}

	fun setListeners(onDelete: () -> Unit, onChangeActiveState: () -> Unit) {
		this.onDelete = onDelete
		this.onChangeActiveState = onChangeActiveState
	}
}