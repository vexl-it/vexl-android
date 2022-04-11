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
	override fun areItemsTheSame(oldItem: Offer, newItem: Offer): Boolean = oldItem.offerId == newItem.offerId

	override fun areContentsTheSame(oldItem: Offer, newItem: Offer): Boolean = oldItem == newItem
}) {

	inner class ViewHolder constructor(
		private val binding: ItemOfferBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: Offer?) {
			binding.offerDescription.text = item?.direction
			binding.priceLimit.text = item?.amount
			binding.paymentMethod.text = item?.paymentMethod
			binding.feeAmount.text = item?.fee
			binding.userName.text = item?.userPublicKey
			binding.location.text = item?.location
			binding.feeGroup.isVisible = item?.fee != null
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