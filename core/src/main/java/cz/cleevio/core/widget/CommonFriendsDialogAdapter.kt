package cz.cleevio.core.widget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.cleevio.core.R
import cz.cleevio.core.databinding.ItemChatCommonFriendBinding
import cz.cleevio.core.utils.setPlaceholders
import cz.cleevio.repository.model.contact.BaseContact

class CommonFriendsDialogAdapter :
	ListAdapter<BaseContact, CommonFriendsDialogAdapter.ViewHolder>(object : DiffUtil.ItemCallback<BaseContact>() {
		override fun areItemsTheSame(oldItem: BaseContact, newItem: BaseContact): Boolean = oldItem.id == newItem.id

		override fun areContentsTheSame(oldItem: BaseContact, newItem: BaseContact): Boolean = oldItem == newItem
	}) {

	inner class ViewHolder constructor(
		private val binding: ItemChatCommonFriendBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: BaseContact) {
			binding.profileImage.load(item.photoUri) {
				setPlaceholders(R.drawable.random_avatar_1)
			}
			binding.name.text = item.name
			binding.description.text = item.getChatDescription(binding.description.context)
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemChatCommonFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
}
