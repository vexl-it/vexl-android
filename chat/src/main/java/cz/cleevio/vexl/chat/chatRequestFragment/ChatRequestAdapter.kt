package cz.cleevio.vexl.chat.chatRequestFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.offer.Location
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.vexl.chat.databinding.ItemChatRequestBinding
import java.math.BigDecimal
import java.time.ZonedDateTime

class ChatRequestAdapter : ListAdapter<ChatMessage, ChatRequestAdapter.ViewHolder>(object : DiffUtil.ItemCallback<ChatMessage>() {
	override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem.uuid == newItem.uuid
	// TODO uuid???

	override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
}) {

	inner class ViewHolder constructor(
		private val binding: ItemChatRequestBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: ChatMessage) {

			//todo: we will also need message and offer
			//fixme: debug data
			var debugDescription = ""
			repeat(4) { debugDescription = "$debugDescription Dlouhy text o offer na nekolik radku, abych videl vetsi card view" }
			val debugOffer = Offer(
				id = 100,
				offerId = "ab123",
				location = listOf(Location(BigDecimal(100.0), BigDecimal(40.0), BigDecimal(1.0))),
				userPublicKey = "",
				offerPublicKey = "",
				offerDescription = debugDescription,
				amountBottomLimit = BigDecimal(100),
				amountTopLimit = BigDecimal(15000),
				feeState = "",
				feeAmount = BigDecimal(0),
				locationState = "",
				paymentMethod = listOf("CASH", "REVOLUT"),
				btcNetwork = listOf(),
				friendLevel = "",
				offerType = "BUY",
				createdAt = ZonedDateTime.now(),
				modifiedAt = ZonedDateTime.now()
			)
			binding.requestMessage.text = "Tohle je zprava od jineho uzivatele"
			binding.offerWidget.bind(debugOffer)
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemChatRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
}