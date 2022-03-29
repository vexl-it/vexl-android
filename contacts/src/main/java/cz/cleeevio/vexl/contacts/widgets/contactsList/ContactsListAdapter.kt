package cz.cleeevio.vexl.contacts.widgets.contactsList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.cleeevio.vexl.contacts.R
import cz.cleeevio.vexl.contacts.databinding.ItemContactBinding
import cz.cleevio.repository.model.contact.BaseContact

class ContactsListAdapter(
	private val onContactImportSwitched: (BaseContact, Boolean) -> Unit
) : ListAdapter<BaseContact, ContactsListAdapter.ViewHolder>(object : DiffUtil.ItemCallback<BaseContact>() {

	override fun areItemsTheSame(oldItem: BaseContact, newItem: BaseContact): Boolean = oldItem.id == newItem.id

	override fun areContentsTheSame(oldItem: BaseContact, newItem: BaseContact): Boolean = oldItem == newItem

}) {

	inner class ViewHolder constructor(
		private val binding: ItemContactBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: BaseContact) {
			binding.run {
				contactImage.load(item.photoUri) {
					crossfade(true)
					fallback(R.drawable.ic_baseline_person_128)
					error(R.drawable.ic_baseline_person_128)
					placeholder(R.drawable.ic_baseline_person_128)
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