package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import cz.cleevio.core.R
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
		binding.priceCurrency.text = "Kč"
		binding.offerType.text = if (item.offerType == "SELL") {
			resources.getString(R.string.offer_to_sell)
		} else {
			resources.getString(R.string.offer_to_buy)
		}
		binding.userName.text = if (item.offerType == "SELL") {
			resources.getString(R.string.marketplace_detail_user_sell, "Unknown friend")
		} else {
			resources.getString(R.string.marketplace_detail_user_buy, "Unknown friend")
		}
		// TODO convert to readable format
		binding.location.text = "${item.location.first().latitude},\n${item.location.first().longitude}"
		binding.userType.text = if (item.friendLevel == "FIRST") {
			resources.getString(R.string.marketplace_detail_friend_first)
		} else {
			resources.getString(R.string.marketplace_detail_friend_second)
		}

		binding.paymentMethodIcons.bind(item.paymentMethod)

		binding.requestBtn.isVisible = requestOffer != null
		binding.requestBtn.setOnClickListener {
			requestOffer?.invoke(item.offerId)
		}
	}

	private fun setupUI() {
		binding = WidgetOfferBinding.inflate(layoutInflater, this)
	}
}