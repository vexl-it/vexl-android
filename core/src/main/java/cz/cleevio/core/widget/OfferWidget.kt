package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferBinding
import cz.cleevio.core.utils.formatAsPercentage
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

class OfferWidget @JvmOverloads constructor(
	context: Context,
	val attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferBinding

	init {
		setupUI()
	}

	fun bind(item: Offer, requestOffer: ((String) -> Unit)? = null, mode: Mode? = null) {
		binding.offerDescription.text = item.offerDescription
		binding.priceLimit.text = "${item.amountTopLimit / BigDecimal(THOUSAND)}k"
		binding.priceCurrency.text = item.currency
		binding.offerType.text = if (item.offerType == "SELL") {
			resources.getString(R.string.offer_to_sell)
		} else {
			resources.getString(R.string.offer_to_buy)
		}
		binding.userName.text = if (mode == Mode.MY_OFFER) {
			context.getString(R.string.offer_my_offer)
		} else {
			if (item.offerType == "SELL") {
				resources.getString(R.string.marketplace_detail_user_sell, "Unknown friend")
			} else {
				resources.getString(R.string.marketplace_detail_user_buy, "Unknown friend")
			}
		}

		binding.location.text = item.location.joinToString(", ") { it.city }

		binding.userType.text = if (mode == Mode.MY_OFFER) {
			context.getString(R.string.offer_added, myOfferFormat.format(item.createdAt))
		} else {
			if (item.friendLevel == "FIRST") {
				resources.getString(R.string.marketplace_detail_friend_first)
			} else {
				resources.getString(R.string.marketplace_detail_friend_second)
			}
		}

		binding.feeDescription.isVisible = item.feeState == "WITH_FEE"
		binding.feeDescription.text =
			resources.getString(R.string.marketplace_detail_fee, item.feeAmount.formatAsPercentage())

		binding.paymentMethod.text = item.paymentMethod.joinToString(", ")
		binding.paymentMethodIcons.bind(item.paymentMethod)

		if (mode == Mode.MY_OFFER) {
			binding.requestBtn.isVisible = false
			binding.editBtn.isVisible = true
		} else {
			binding.requestBtn.isVisible = true
			binding.editBtn.isVisible = false

			binding.requestBtn.isVisible = requestOffer != null
			binding.requestBtn.text = if (item.isRequested) {
				context.getString(R.string.offer_requested)
			} else {
				context.getString(R.string.offer_request)
			}
			binding.requestBtn.setTextColor(
				if (item.isRequested) {
					context.getColor(R.color.white)
				} else {
					context.getColor(R.color.black)
				}
			)
			binding.requestBtn.icon = if (item.isRequested) {
				context.getDrawable(R.drawable.ic_succesfull_blue)
			} else {
				context.getDrawable(R.drawable.ic_eye)
			}
			binding.requestBtn.isEnabled = !item.isRequested
		}
		binding.requestBtn.setOnClickListener {
			requestOffer?.invoke(item.offerId)
		}
		binding.editBtn.setOnClickListener {
			requestOffer?.invoke(item.offerId)
		}
	}

	private fun setupUI() {
		binding = WidgetOfferBinding.inflate(layoutInflater, this)

		val widgetMode = getMode()
		binding.userInformationGroup.isVisible = widgetMode == Mode.MARKETPLACE
		if (widgetMode == Mode.CHAT) {
			binding.offerWrapper.run {
				setCardBackgroundColor(resources.getColor(R.color.gray_6, null))
				cardElevation = 0.0f
			}
		}
	}

	private fun getMode(): Mode {
		val a = context.theme.obtainStyledAttributes(
			attrs,
			R.styleable.OfferWidget,
			0, 0
		)
		val value = a.getInt(R.styleable.OfferWidget_widget_mode, 0)
		return Mode.values()[value]
	}

	enum class Mode {
		MARKETPLACE, CHAT, MY_OFFER
	}

	companion object {
		const val THOUSAND = 1000
		val myOfferFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd. MM. yyyy")
	}
}
