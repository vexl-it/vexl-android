package cz.cleevio.profile.groupFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.cleevio.core.R
import cz.cleevio.profile.databinding.ItemGroupBinding
import cz.cleevio.repository.model.group.Group

class GroupAdapter constructor(
	val onLeaveGroup: (Group) -> Unit,
) : ListAdapter<Group, GroupAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Group>() {
	override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean = oldItem.groupUuid == newItem.groupUuid

	override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean = oldItem == newItem
}) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	inner class ViewHolder constructor(
		private val binding: ItemGroupBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: Group) {
			binding.groupName.text = item.name
			binding.groupMembers.text = binding.groupMembers.context.getString(
				R.string.groups_item_members, item.memberCount.toString()
			)
			binding.groupLogo.load(item.logoUrl) {
				crossfade(true)
				fallback(R.drawable.random_avatar_5)
				error(R.drawable.random_avatar_5)
				placeholder(R.drawable.random_avatar_5)
			}

			binding.leaveBtn.setOnClickListener {
				onLeaveGroup(item)
			}
		}
	}
}
