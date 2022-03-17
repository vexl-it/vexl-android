package cz.cleeevio.vexl.contacts.contactsListFragment

import androidx.lifecycle.lifecycleScope
import cz.cleeevio.vexl.contacts.R
import cz.cleeevio.vexl.contacts.databinding.FragmentContactsListBinding
import cz.cleevio.core.utils.viewBinding
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactsListFragment : BaseFragment(R.layout.fragment_contacts_list) {

	private val binding by viewBinding(FragmentContactsListBinding::bind)
	override val viewModel by viewModel<ContactsListViewModel>()

	override fun bindObservers() {
		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.contacts.collect {
				binding.contactsListWidget.setupData(it)
			}
		}
	}

	override fun initView() {
	}

}