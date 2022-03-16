package cz.cleeevio.vexl.contacts.contactsListFragment

import cz.cleeevio.vexl.contacts.R
import cz.cleeevio.vexl.contacts.databinding.FragmentContactsListBinding
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactsListFragment : BaseFragment(R.layout.fragment_contacts_list) {

	private val binding by viewBinding(FragmentContactsListBinding::bind)
	override val viewModel by viewModel<ContactsListViewModel>()

	override fun bindObservers() {
		TODO("Not yet implemented")
	}

	override fun initView() {
		TODO("Not yet implemented")
	}

}