package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cleeevio.vexl.marketplace.databinding.ItemOfferBinding
import cz.cleevio.core.widget.OfferWidget
import cz.cleevio.repository.model.offer.Offer

class OffersAdapter constructor(
	val requestOffer: (String) -> Unit,
	val offerMode: OfferWidget.Mode = OfferWidget.Mode.MARKETPLACE
) : ListAdapter<Offer, OffersAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Offer>() {
	override fun areItemsTheSame(oldItem: Offer, newItem: Offer): Boolean = oldItem.offerId == newItem.offerId

	override fun areContentsTheSame(oldItem: Offer, newItem: Offer): Boolean = oldItem == newItem
}) {

	inner class ViewHolder constructor(
		private val binding: ItemOfferBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: Offer) {
			binding.offerWidget.bind(item, requestOffer, offerMode)
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