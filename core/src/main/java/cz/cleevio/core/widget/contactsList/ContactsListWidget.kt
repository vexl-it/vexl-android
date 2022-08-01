package cz.cleevio.core.widget.contactsList

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import cz.cleevio.resources.R
import cz.cleevio.core.databinding.WidgetContactsImportListBinding
import cz.cleevio.core.model.OpenedFromScreen
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
		onDeselectAllClicked: () -> Unit
	) {
		adapter = ContactsListAdapter(onContactImportSwitched)
		binding.contactsList.adapter = adapter

		binding.deselectAllBtn.setOnClickListener {
			onDeselectAllClicked()
		}
	}

	fun setupData(
		contacts: List<BaseContact>,
		openedFromScreen: OpenedFromScreen
	) {
		adapter.submitList(contacts)
		binding.contactsList.isVisible = contacts.isNotEmpty()
		binding.emptyListInfo.isVisible = contacts.isEmpty()
		binding.deselectAllBtn.text =
			if (openedFromScreen == OpenedFromScreen.ONBOARDING) {
				resources.getString(R.string.import_contacts_deselect)
			} else {
				resources.getString(R.string.import_contacts_select)
			}
	}
}