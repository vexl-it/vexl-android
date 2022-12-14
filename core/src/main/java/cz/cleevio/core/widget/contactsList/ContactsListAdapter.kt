package cz.cleevio.core.widget.contactsList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.cleevio.core.R
import cz.cleevio.core.databinding.ItemContactBinding
import cz.cleevio.core.utils.setPlaceholders
import cz.cleevio.repository.model.contact.BaseContact

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

		fun bind(item: BaseContact) {
			binding.run {
				contactImportCheckbox.setOnCheckedChangeListener(null)

				contactImage.load(item.photoUri) {
					setPlaceholders(R.drawable.random_avatar_3)
				}
				contactName.text = item.name
				contactIdentifier.text = item.getIdentifier()
				contactImportCheckbox.isChecked = item.markedForUpload

				contactImportCheckbox.setOnCheckedChangeListener { _, isChecked -> onContactImportSwitched(item, isChecked) }
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
		ViewHolder(
			ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
}
