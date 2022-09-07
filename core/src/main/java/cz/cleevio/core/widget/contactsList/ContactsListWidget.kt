package cz.cleevio.core.widget.contactsList

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import cz.cleevio.core.databinding.WidgetContactsImportListBinding
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.resources.R
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater
import org.koin.core.component.KoinComponent
import timber.log.Timber

class ContactsListWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

	private lateinit var binding: WidgetContactsImportListBinding
	private lateinit var adapter: ContactsListAdapter
	private var _contacts: List<BaseContact> = emptyList()
	private var isDeselectAll: Boolean = true

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetContactsImportListBinding.inflate(layoutInflater, this)
	}

	fun setupListeners(
		onContactImportSwitched: (BaseContact, Boolean) -> Unit,
		onDeselectAllClicked: () -> Unit,
		onSelectAllClicked: () -> Unit,
	) {
		adapter = ContactsListAdapter(onContactImportSwitched)
		binding.contactsList.adapter = adapter

		binding.searchNameInput.setOnClickListener {
			binding.searchNameInput.isIconified = false
		}

		binding.searchNameInput.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean {
				val filteredContacts = _contacts.filter { it.name.lowercase().contains(query.lowercase()) }
				adapter.submitList(filteredContacts)

				return false
			}

			override fun onQueryTextChange(newText: String): Boolean {
				val filteredContacts = _contacts.filter { it.name.lowercase().contains(newText.lowercase()) }
				adapter.submitList(filteredContacts)

				return false
			}
		})

		binding.deselectAllBtn.setOnClickListener {
			isDeselectAll = if (isDeselectAll) {
				onDeselectAllClicked()
				false
			} else {
				onSelectAllClicked()
				true
			}
			setupDeselectAllBtn()
		}
	}

	fun setupData(
		contacts: List<BaseContact>
	) {
		val query = binding.searchNameInput.query.toString().lowercase()
		if (query.isNotEmpty() && query.isNotBlank()) {
			val filteredContacts = _contacts.filter { it.name.lowercase().contains(query) }
			adapter.submitList(filteredContacts)
		} else {
			Timber.tag("ContactSync").d("Will be submitting ${contacts.size}")
			adapter.submitList(contacts)
		}

		_contacts = contacts
		binding.contactsList.isVisible = contacts.isNotEmpty()
		binding.emptyListInfo.isVisible = contacts.isEmpty()
		setupDeselectAllBtn()
	}

	private fun setupDeselectAllBtn() {
		binding.deselectAllBtn.text = if (isDeselectAll) {
			resources.getString(R.string.import_contacts_deselect)
		} else {
			resources.getString(R.string.import_contacts_select)
		}
	}
}