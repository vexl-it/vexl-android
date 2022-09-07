package cz.cleevio.core.widget.contactsList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.cleevio.core.R
import cz.cleevio.core.databinding.ItemContactBinding
import cz.cleevio.repository.model.contact.BaseContact
import timber.log.Timber

class ContactsListAdapter(
	private val onContactImportSwitched: (BaseContact, Boolean) -> Unit
) : ListAdapter<BaseContact, ContactsListAdapter.ViewHolder>(object : DiffUtil.ItemCallback<BaseContact>() {

	override fun areItemsTheSame(oldItem: BaseContact, newItem: BaseContact): Boolean =
		oldItem.id == newItem.id

	override fun areContentsTheSame(oldItem: BaseContact, newItem: BaseContact): Boolean =
		oldItem.id == newItem.id && oldItem.markedForUpload == newItem.markedForUpload
}) {

	inner class ViewHolder constructor(
		private val binding: ItemContactBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: BaseContact, position: Int) {
			binding.run {
				contactImportCheckbox.setOnCheckedChangeListener(null)

				contactImage.load(item.photoUri) {
					crossfade(true)
					fallback(R.drawable.random_avatar_3)
					error(R.drawable.random_avatar_3)
					placeholder(R.drawable.random_avatar_3)
				}
				contactName.text = item.name
				contactIdentifier.text = item.getIdentifier()
				contactImportCheckbox.isChecked = item.markedForUpload

				contactImportCheckbox.setOnCheckedChangeListener { _, isChecked -> onContactImportSwitched(item, isChecked) }

				Timber.tag("ContactSync").d("binding ${position + 1} with name ${item.name}")
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (position == 0) {
			Timber.tag("ContactSync").d("size: ${itemCount}")
		}
		holder.bind(getItem(position), position)
	}
}
