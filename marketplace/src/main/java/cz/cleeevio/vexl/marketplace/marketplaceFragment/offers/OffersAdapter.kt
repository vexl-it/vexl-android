package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cleeevio.vexl.marketplace.databinding.ItemOfferBinding
import cz.cleevio.repository.model.offer.Offer

class OffersAdapter : ListAdapter<Offer, OffersAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Offer>() {
	override fun areItemsTheSame(oldItem: Offer, newItem: Offer): Boolean = oldItem.id == newItem.id

	override fun areContentsTheSame(oldItem: Offer, newItem: Offer): Boolean = oldItem == newItem
}) {

	inner class ViewHolder constructor(
		private val binding: ItemOfferBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: Offer?) {
			binding.offerDescription.text = "I’ll be wearing a red hat, Don’t text me before 9am — I love to sleep..."
			binding.priceLimit.text = "up to \$10k"
			binding.paymentMethod.text = "Revolut"
			binding.feeAmount.text = "Wants \$30 fee per transaction"
			binding.userName.text = "Facebook friend"
			binding.location.text = "Prague 7"
			binding.feeGroup.isVisible = true
		}

	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemOfferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

}