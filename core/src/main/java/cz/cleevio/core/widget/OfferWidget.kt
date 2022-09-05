package cz.cleevio.core.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil.load
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferBinding
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.BuySellColorizer.colorizeTransactionType
import cz.cleevio.core.utils.formatAsPercentage
import cz.cleevio.repository.RandomUtils
import cz.cleevio.repository.model.Currency.Companion.getCurrencySymbol
import cz.cleevio.repository.model.Currency.Companion.mapStringToCurrency
import cz.cleevio.repository.model.group.Group
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.format.DateTimeFormatter
import java.util.*

const val LOCATION_DISPLAY_LIMIT = 3

class OfferWidget @JvmOverloads constructor(
	context: Context,
	val attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferBinding

	init {
		setupUI()
	}

	@Suppress("LongMethod", "ComplexMethod")
	fun bind(item: Offer, requestOffer: ((String) -> Unit)? = null, mode: Mode? = null, group: Group? = null) {
		binding.card.offerDescription.text = item.offerDescription
		group?.let {
			binding.card.groupInfo.text = resources.getString(R.string.offer_widget_groups_info, group.name)

			//todo: later take sticker from group?
			binding.card.groupSticker.load(R.drawable.ic_sticker) {
				crossfade(true)
				fallback(R.drawable.ic_sticker)
				error(R.drawable.ic_sticker)
				placeholder(R.drawable.ic_sticker)
			}
		}
		binding.card.groupInfo.isVisible = group != null
		binding.card.groupSticker.isVisible = group != null

		val priceLimit = item.amountTopLimit.divide(
			BigDecimal(THOUSAND),
			1,
			RoundingMode.HALF_UP
		)

		binding.card.priceLimit.text =
			"${DecimalFormat("###,###.#", DecimalFormatSymbols(Locale.US)).format(priceLimit)}k"

		binding.card.priceCurrency.text = item.currency.mapStringToCurrency().getCurrencySymbol(context)


		if (item.userAvatar.isNullOrBlank()) {
			Timber.tag("ASDX").d("userAvatarId ${item.userAvatarId}")
			//either select random drawable
			if (item.userAvatarId != null) {
				binding.profileImage.load(
					RandomUtils.selectRandomImage(context, item.userAvatarId!!)
				)
			} else {
				binding.profileImage.load(
					RandomUtils.selectRandomImage(context)
				)
			}

		} else {
			//or load user avatar from url
			binding.profileImage.load(
				item.userAvatar
			)
		}
		val generatedUsername = item.userName ?: RandomUtils.generateName()

		binding.card.offerType.text = if (item.offerType == OfferType.SELL.name) {
			resources.getString(R.string.offer_to_sell)
		} else {
			resources.getString(R.string.offer_to_buy)
		}

		when (mode) {
			Mode.MARKETPLACE -> {
				val isSell = item.offerType == OfferType.SELL.name && !item.isMine ||
					item.offerType == OfferType.BUY.name && item.isMine
				if (isSell) {
					colorizeTransactionType(
						resources.getString(R.string.marketplace_detail_user_sell, "$generatedUsername "),
						generatedUsername,
						binding.userName,
						if (item.isRequested) R.color.gray_3 else R.color.pink_100
					)
				} else {
					colorizeTransactionType(
						resources.getString(R.string.marketplace_detail_user_buy, "$generatedUsername "),
						generatedUsername,
						binding.userName,
						if (item.isRequested) R.color.gray_3 else R.color.green_100
					)
				}
			}
			Mode.CHAT -> {
				val isSell = item.offerType == OfferType.SELL.name && !item.isMine ||
					item.offerType == OfferType.BUY.name && item.isMine
				if (isSell) {
					colorizeTransactionType(
						resources.getString(R.string.marketplace_detail_user_sell, "$generatedUsername "),
						generatedUsername,
						binding.userName,
						R.color.pink_100
					)
				} else {
					colorizeTransactionType(
						resources.getString(R.string.marketplace_detail_user_buy, "$generatedUsername "),
						generatedUsername,
						binding.userName,
						R.color.green_100
					)
				}
			}
			Mode.MY_OFFER -> {
				binding.userName.text = context.getString(R.string.offer_my_offer)
			}
			null -> {
				//fallback
			}
		}

		//location icons
		val locationValues = mutableListOf<String>().apply {
			if (item.location.isNotEmpty()) {
				add(LocationButtonSelected.IN_PERSON.name)
			}

			if (item.locationState == LocationButtonSelected.ONLINE.name) {
				add(LocationButtonSelected.ONLINE.name)
			}
		}
		binding.card.locationIcons.bind(locationValues)

		//location text + duplicity prevention
		val locations = item.location.map { it.city }.toSet()
		val cities = locations.take(LOCATION_DISPLAY_LIMIT).joinToString(", ") { it }
		binding.card.location.text = if (item.locationState == LocationButtonSelected.ONLINE.name) {
			"$cities, ${resources.getString(R.string.widget_location_online)}"
		} else {
			cities
		}

		binding.userType.text = if (mode == Mode.MY_OFFER) {
			context.getString(R.string.offer_added, myOfferFormat.format(item.createdAt))
		} else {
			when {
				group != null -> {
					resources.getString(R.string.offer_widget_groups, group.name)
				}
				item.friendLevel == FriendLevel.FIRST_DEGREE.name -> {
					resources.getString(R.string.marketplace_detail_friend_first)
				}
				item.friendLevel == FriendLevel.SECOND_DEGREE.name -> {
					resources.getString(R.string.marketplace_detail_friend_second)
				}
				//fallback
				else -> {
					""
				}
			}
		}

		binding.card.feeDescription.isVisible = item.feeState == Fee.WITH_FEE.name
		binding.card.feeDescription.text =
			resources.getString(R.string.marketplace_detail_fee, item.feeAmount.formatAsPercentage())

		binding.card.paymentMethod.text = item.paymentMethod.joinToString(", ") {
			it.lowercase().replaceFirstChar { a -> a.uppercase() }
		}
		binding.card.paymentMethodIcons.bind(item.paymentMethod)

		when (mode) {
			Mode.MARKETPLACE -> {
				binding.requestBtn.isVisible = true
				binding.editBtn.isVisible = false

				binding.requestBtn.isVisible = requestOffer != null
				binding.requestBtn.text = if (item.isRequested) {
					context.getString(R.string.offer_requested)
				} else {
					context.getString(R.string.offer_request)
				}

				setUiColor(item.isRequested)
			}
			Mode.CHAT -> {
				binding.requestBtn.isVisible = false
				binding.editBtn.isVisible = false
			}
			Mode.MY_OFFER -> {
				binding.requestBtn.isVisible = false
				binding.editBtn.isVisible = true
			}
			Mode.CHAT_REQUEST -> {
				binding.requestBtn.isVisible = false
				binding.editBtn.isVisible = true
			}
			null -> {
				//fallback
			}
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
		if (widgetMode == Mode.CHAT || widgetMode == Mode.CHAT_REQUEST) {
			binding.card.offerWrapper.run {
				setCardBackgroundColor(resources.getColor(R.color.gray_6, null))
				cardElevation = 0.0f
			}
		}
		binding.card.yourOffer.isVisible = widgetMode == Mode.CHAT_REQUEST
		binding.arrowImage.isVisible = widgetMode != Mode.CHAT_REQUEST && widgetMode != Mode.CHAT
		binding.endDivider.isVisible = widgetMode != Mode.CHAT_REQUEST
		binding.endDividerChatRequest.isVisible = widgetMode == Mode.CHAT_REQUEST
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

	private fun setUiColor(isRequested: Boolean) {
		binding.requestBtn.setTextColor(
			if (isRequested) {
				context.getColor(R.color.white)
			} else {
				context.getColor(R.color.black)
			}
		)
		binding.requestBtn.icon = if (isRequested) {
			binding.requestBtn.iconTint = null
			AppCompatResources.getDrawable(context, R.drawable.ic_succesfull_blue)
		} else {
			binding.requestBtn.iconTint = ColorStateList.valueOf(resources.getColor(R.color.black, null))
			AppCompatResources.getDrawable(context, R.drawable.ic_eye)
		}

		binding.card.offerWrapper.run {
			setCardBackgroundColor(
				if (isRequested) resources.getColor(R.color.gray_1, null) else resources.getColor(R.color.gray_6, null)
			)
			cardElevation = 0.0f
		}

		binding.card.offerDescription.setTextColor(
			if (isRequested) resources.getColor(R.color.gray_3, null) else resources.getColor(R.color.black, null)
		)

		binding.card.feeDescription.setTextColor(
			if (isRequested) resources.getColor(R.color.gray_3, null) else resources.getColor(R.color.black, null)
		)

		binding.arrowImage.setColorFilter(
			ContextCompat.getColor(
				context,
				if (isRequested) R.color.gray_1 else R.color.white
			)
		)

		if (isRequested) {
			binding.profileImage.setColorFilter(
				ContextCompat.getColor(context, R.color.gray_1),
				android.graphics.PorterDuff.Mode.MULTIPLY
			)
		} else {
			binding.profileImage.clearColorFilter()
		}

		binding.requestBtn.isEnabled = !isRequested
	}

	enum class Mode {
		MARKETPLACE, CHAT, CHAT_REQUEST, MY_OFFER
	}

	enum class Fee {
		WITHOUT_FEE, WITH_FEE
	}

	companion object {
		const val THOUSAND = 1000
		const val ROUND_PRECISION = 2
		val myOfferFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd. MM. yyyy")
	}
}
