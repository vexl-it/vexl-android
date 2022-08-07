package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetPaymentMethodIconsBinding
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater

class PaymentMethodIconsWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetPaymentMethodIconsBinding

	init {
		setupUI()
	}

	fun bind(paymentTypes: List<String>) {
		binding.oneMethodGroup.isVisible = false
		binding.twoMethodsGroup.isVisible = false
		binding.threeMethodsGroup.isVisible = false

		@Suppress("MagicNumber")
		when (paymentTypes.size) {
			1 -> {
				binding.oneMethodFirst.setImageResource(getIcon(paymentTypes[0]))
				binding.oneMethodGroup.isVisible = true
			}
			2 -> {
				binding.twoMethodsFirst.setImageResource(getIcon(paymentTypes[0]))
				binding.twoMethodsSecond.setImageResource(getIcon(paymentTypes[1]))
				binding.twoMethodsGroup.isVisible = true
			}
			3 -> {
				binding.threeMethodsFirst.setImageResource(getIcon(paymentTypes[0]))
				binding.threeMethodsSecond.setImageResource(getIcon(paymentTypes[1]))
				binding.threeMethodsThird.setImageResource(getIcon(paymentTypes[2]))
				binding.threeMethodsGroup.isVisible = true
			}
			else -> {
			} //TODO error
		}
	}

	private fun getIcon(paymentType: String): Int {
		return when (paymentType) {
			"Revolut" -> R.drawable.ic_revolut
			"Bank" -> R.drawable.ic_bank
			"Cash" -> R.drawable.ic_cash
			else -> R.drawable.ic_map_pin // TODO error
		}
	}

	private fun setupUI() {
		binding = WidgetPaymentMethodIconsBinding.inflate(layoutInflater, this)
	}
}
