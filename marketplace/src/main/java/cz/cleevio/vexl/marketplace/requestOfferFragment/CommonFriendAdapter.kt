package cz.cleevio.vexl.marketplace.requestOfferFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.cleevio.core.R
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.vexl.marketplace.databinding.ItemRequestOfferCommonFriendBinding

class CommonFriendAdapter
	: ListAdapter<BaseContact, CommonFriendAdapter.ViewHolder>(object : DiffUtil.ItemCallback<BaseContact>() {
	override fun areItemsTheSame(oldItem: BaseContact, newItem: BaseContact): Boolean = oldItem.id == newItem.id

	override fun areContentsTheSame(oldItem: BaseContact, newItem: BaseContact): Boolean = oldItem == newItem
}) {

	inner class ViewHolder constructor(
		private val binding: ItemRequestOfferCommonFriendBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: BaseContact) {
			binding.profileImage.load(item.photoUri) {
				crossfade(true)
				fallback(R.drawable.random_avatar_2)
				error(R.drawable.random_avatar_2)
				placeholder(R.drawable.random_avatar_2)
			}
			binding.name.text = item.name
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemRequestOfferCommonFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

}
