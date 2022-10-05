package cz.cleevio.profile.logFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cleevio.network.utils.LogData
import cz.cleevio.profile.databinding.ItemLogBinding

class LogAdapter : ListAdapter<LogData, LogAdapter.ViewHolder>(object : DiffUtil.ItemCallback<LogData>() {
	override fun areItemsTheSame(oldItem: LogData, newItem: LogData): Boolean =
		oldItem.timestamp == newItem.timestamp

	override fun areContentsTheSame(oldItem: LogData, newItem: LogData): Boolean =
		oldItem == newItem
}) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	inner class ViewHolder constructor(
		private val binding: ItemLogBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: LogData) {

			binding.logText.text = item.log
		}
	}
}