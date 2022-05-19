package cz.cleevio.vexl.chat.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import coil.load
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.model.user.User
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.WidgetRequestViewBinding
import lightbase.core.extensions.layoutInflater

class RequestViewWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetRequestViewBinding
	var onAccept: (User) -> Unit = {}
	var onBlock: (User) -> Unit = {}

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetRequestViewBinding.inflate(layoutInflater, this)
	}

	fun setData(users: List<User>, message: String, offer: Offer) {
		//display data for first user
		users.firstOrNull()?.let { user ->
			binding.userImage.load(user.avatar) {
				crossfade(true)
				fallback(R.drawable.ic_baseline_person_128)
				error(R.drawable.ic_baseline_person_128)
				placeholder(R.drawable.ic_baseline_person_128)
			}

			binding.userName.text = user.username
			binding.userText.text = message

			binding.requestOfferDescription.text = offer.offerDescription

			//add listeners
			binding.requestAcceptBtn.setOnClickListener { onAccept(users.first()) }
			binding.requestBlockBtn.setOnClickListener { onBlock(users.first()) }

			handleCardCount(users, binding.lowestCard, binding.middleCard)
		}
	}

	private fun handleCardCount(users: List<User>, lowestCardView: CardView, middleCardView: CardView) {
		lowestCardView.isVisible = users.size >= 3
		middleCardView.isVisible = users.size >= 2
	}
}