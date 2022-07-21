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
			//todo: add members count when we have it
			binding.groupMembers.text = "TODO: ${item.code.toString()}"
			binding.groupLogo.load(item.logoUrl) {
				crossfade(true)
				//todo: ask for placeholders?
				fallback(R.drawable.ic_baseline_person_128)
				error(R.drawable.ic_baseline_person_128)
				placeholder(R.drawable.ic_baseline_person_128)
			}

			binding.leaveBtn.setOnClickListener {
				onLeaveGroup(item)
			}
		}
	}
}