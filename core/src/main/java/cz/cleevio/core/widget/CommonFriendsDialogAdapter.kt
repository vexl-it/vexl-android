package cz.cleevio.core.widget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.cleevio.core.R
import cz.cleevio.core.databinding.ItemChatCommonFriendBinding
import cz.cleevio.repository.model.contact.CommonFriend

class CommonFriendsDialogAdapter :
	ListAdapter<CommonFriend, CommonFriendsDialogAdapter.ViewHolder>(object : DiffUtil.ItemCallback<CommonFriend>() {
		override fun areItemsTheSame(oldItem: CommonFriend, newItem: CommonFriend): Boolean = oldItem.id == newItem.id

		override fun areContentsTheSame(oldItem: CommonFriend, newItem: CommonFriend): Boolean = oldItem == newItem
	}) {

	inner class ViewHolder constructor(
		private val binding: ItemChatCommonFriendBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: CommonFriend) {
			binding.profileImage.load(item.userAvatar) {
				crossfade(true)
				fallback(R.drawable.ic_baseline_person_128)
				error(R.drawable.ic_baseline_person_128)
				placeholder(R.drawable.ic_baseline_person_128)
			}
			binding.name.text = item.name
			binding.description.text = "TODO"
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