package cz.cleevio.vexl.marketplace

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.cleevio.core.R
import cz.cleevio.repository.model.group.Group
import cz.cleevio.vexl.marketplace.databinding.ItemSelectGroupBinding
import okhttp3.internal.notifyAll

class SelectGroupAdapter  : ListAdapter<Group, SelectGroupAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Group>() {
	override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean = oldItem.groupUuid == newItem.groupUuid

	override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean = oldItem == newItem
}) {

	private val selectedGroups: MutableSet<String> = mutableSetOf()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemSelectGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	fun getSelectedGroupUuids(): List<String> = selectedGroups.toList()

	fun setSelectedGroupUuids(uuids: List<String>) {
		selectedGroups.clear()
		selectedGroups.addAll(uuids)
		this.notifyDataSetChanged()
	}

	inner class ViewHolder constructor(
		private val binding: ItemSelectGroupBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: Group) {
			binding.groupName.text = item.name
			binding.groupLogo.load(item.logoUrl) {
				crossfade(true)
				fallback(R.drawable.ic_groups)
				error(R.drawable.ic_groups)
				placeholder(R.drawable.ic_groups)
			}
			binding.groupCheck.isVisible = selectedGroups.contains(item.groupUuid)
			checkTextColor(item)

			binding.container.setOnClickListener {
				if (selectedGroups.contains(item.groupUuid)) {
					selectedGroups.remove(item.groupUuid)
				} else {
					selectedGroups.add(item.groupUuid)
				}

				binding.groupCheck.isVisible = selectedGroups.contains(item.groupUuid)
				checkTextColor(item)
			}
		}

		private fun checkTextColor(item: Group) {
			if (selectedGroups.contains(item.groupUuid)) {
				binding.groupName.setTextColor(ContextCompat.getColor(binding.container.context, R.color.white))
			} else {
				binding.groupName.setTextColor(ContextCompat.getColor(binding.container.context, R.color.gray_3))
			}
		}
	}
}