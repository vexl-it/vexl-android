package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import cz.cleevio.core.databinding.WidgetOfferBinding
import cz.cleevio.repository.model.offer.Offer
import lightbase.core.extensions.layoutInflater
import java.math.BigDecimal

class OfferWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferBinding

	init {
		setupUI()
	}

	fun bind(item: Offer, requestOffer: ((String) -> Unit)?) {
		binding.offerDescription.text = item.offerDescription
		binding.priceLimit.text = "${item.amountTopLimit / BigDecimal(1000)}k"
		binding.paymentMethod.text = "${item.paymentMethod.joinToString()}"
//		binding.feeAmount.text = item.feeAmount.toString()
		binding.userName.text = "Unknown friend"
//		binding.location.text = "${item.location.first().latitude}, ${item.location.first().longitude}"
		binding.userType.text = "Friend of friend"
//		binding.feeGroup.isVisible = item.feeAmount != null

		binding.requestBtn.isVisible = requestOffer != null
		binding.requestBtn.setOnClickListener {
			requestOffer?.invoke(item.offerId)
		}
	}

	private fun setupUI() {
		binding = WidgetOfferBinding.inflate(layoutInflater, this)
	}
}