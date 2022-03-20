package cz.cleeevio.vexl.contacts.widgets.contactsList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import cz.cleeevio.vexl.contacts.databinding.ItemContactBinding
import cz.cleevio.repository.model.contact.Contact

class ContactsListAdapter(
	private val onContactImportSwitched: (Contact, Boolean) -> Unit
) : ListAdapter<Contact, ContactsListAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Contact>() {

	override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean = oldItem.id == newItem.id

	override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean = oldItem == newItem

}) {

	inner class ViewHolder constructor(
		private val binding: ItemContactBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: Contact) {
			binding.run {
				contactImage.load(item.photoUri)
				contactName.text = item.name
				contactPhone.text = item.phoneNumber

				contactImportCheckbox.setOnCheckedChangeListener { buttonView, isChecked -> onContactImportSwitched(item, isChecked) }
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