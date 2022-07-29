package cz.cleevio.core.widget.contactsList

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.databinding.WidgetContactsImportListBinding
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater
import org.koin.core.component.KoinComponent

class ContactsListWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

	private lateinit var binding: WidgetContactsImportListBinding
	private lateinit var adapter: ContactsListAdapter

	init {
		setupUI()
	}

	private fun setupUI() {
		binding = WidgetContactsImportListBinding.inflate(layoutInflater, this)
	}

	fun setupListeners(
		onContactImportSwitched: (BaseContact, Boolean) -> Unit,
		onUnselectAllClicked: () -> Unit
	) {
		adapter = ContactsListAdapter(onContactImportSwitched)
		binding.contactsList.adapter = adapter

		binding.deselectAllBtn.setOnClickListener {
			onUnselectAllClicked()
		}
	}

	fun setupData(
		contacts: List<BaseContact>
	) {
		adapter.submitList(contacts)
	}

}