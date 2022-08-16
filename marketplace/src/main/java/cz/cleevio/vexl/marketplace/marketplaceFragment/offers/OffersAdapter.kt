package cz.cleevio.vexl.marketplace.marketplaceFragment.offers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cleevio.core.widget.OfferWidget
import cz.cleevio.repository.model.offer.OfferWithGroup
import cz.cleevio.vexl.marketplace.databinding.ItemOfferBinding

class OffersAdapter(
	val requestOffer: (String) -> Unit,
	val offerMode: OfferWidget.Mode = OfferWidget.Mode.MARKETPLACE
) : ListAdapter<OfferWithGroup, OffersAdapter.ViewHolder>(object : DiffUtil.ItemCallback<OfferWithGroup>() {
	override fun areItemsTheSame(oldItem: OfferWithGroup, newItem: OfferWithGroup): Boolean = oldItem.offer.offerId == newItem.offer.offerId

	override fun areContentsTheSame(oldItem: OfferWithGroup, newItem: OfferWithGroup): Boolean = oldItem == newItem
}) {

	inner class ViewHolder constructor(
		private val binding: ItemOfferBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: OfferWithGroup) {
			binding.offerWidget.bind(
				item = item.offer,
				requestOffer = requestOffer,
				mode = offerMode,
				group = item.group
			)
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