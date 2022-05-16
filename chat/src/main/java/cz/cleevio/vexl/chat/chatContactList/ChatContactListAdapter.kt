package cz.cleevio.vexl.chat.chatContactList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.cleevio.repository.model.user.User
import cz.cleevio.vexl.chat.databinding.ItemChatContactBinding

class ChatContactListAdapter constructor(
	val chatWithUser: (User) -> Unit
) : ListAdapter<User, ChatContactListAdapter.ViewHolder>(object : DiffUtil.ItemCallback<User>() {
	override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.id == newItem.id

	override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
}) {

	inner class ViewHolder constructor(
		private val binding: ItemChatContactBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: User) {
			binding.chatContactIcon.load(item.avatar)
			binding.chatContactName.text = item.username
			binding.chatLastMessage.text = "There will be message later"    //todo: connect to Message Dao or something

			binding.container.setOnClickListener {
				chatWithUser(item)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemChatContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

}