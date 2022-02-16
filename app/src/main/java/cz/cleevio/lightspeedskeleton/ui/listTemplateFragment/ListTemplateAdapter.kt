package cz.cleevio.lightspeedskeleton.ui.listTemplateFragment

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cleevio.lightspeedskeleton.databinding.ViewHeaderTemplateBinding
import cz.cleevio.lightspeedskeleton.databinding.ViewListItemTemplateBinding
import lightbase.core.extensions.layoutInflater

class ListTemplateAdapter constructor(
	val onItemClick: (Int) -> Unit
) : ListAdapter<ListTemplateModel, RecyclerView.ViewHolder>(
	object : DiffUtil.ItemCallback<ListTemplateModel>() {
		override fun areItemsTheSame(oldItem: ListTemplateModel, newItem: ListTemplateModel): Boolean {
			return when {
				oldItem is ListTemplateModel.ListItem && newItem is ListTemplateModel.ListItem ->
					oldItem.data.id == newItem.data.id

				oldItem is ListTemplateModel.Header && newItem is ListTemplateModel.Header ->
					oldItem.title == newItem.title

				else -> false
			}
		}

		override fun areContentsTheSame(oldItem: ListTemplateModel, newItem: ListTemplateModel): Boolean =
			oldItem == newItem
	}
) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return when (viewType) {
			ListTemplateModel.TYPE_LIST_ITEM ->
				ListItemViewHolder(ViewListItemTemplateBinding.inflate(parent.layoutInflater, parent, false))
			ListTemplateModel.TYPE_HEADER ->
				HeaderViewHolder(ViewHeaderTemplateBinding.inflate(parent.layoutInflater, parent, false))
			else -> throw IllegalArgumentException("Unknown type")
		}
	}

	override fun getItemViewType(position: Int): Int {
		return when {
			getItem(position) is ListTemplateModel.ListItem ->
				ListTemplateModel.TYPE_LIST_ITEM
			getItem(position) is ListTemplateModel.Header ->
				ListTemplateModel.TYPE_HEADER
			else -> throw IllegalArgumentException("Unknown type")
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is ListItemViewHolder ->
				holder.bind(getItem(position) as ListTemplateModel.ListItem)
			is HeaderViewHolder ->
				holder.bind(getItem(position) as ListTemplateModel.Header)
		}
	}

	inner class ListItemViewHolder(
		private val binding: ViewListItemTemplateBinding
	) : RecyclerView.ViewHolder(binding.root) {
		fun bind(item: ListTemplateModel.ListItem) = with(binding) {
			listItem.text = item.data.content

			listItem.setOnClickListener {
				onItemClick.invoke(item.data.id)
			}
		}
	}

	inner class HeaderViewHolder(
		private val binding: ViewHeaderTemplateBinding
	) : RecyclerView.ViewHolder(binding.root) {
		fun bind(header: ListTemplateModel.Header) = with(binding) {
			title.text = header.title
		}
	}
}
