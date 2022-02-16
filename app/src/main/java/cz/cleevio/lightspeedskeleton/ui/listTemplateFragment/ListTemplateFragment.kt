package cz.cleevio.lightspeedskeleton.ui.listTemplateFragment

import cz.cleevio.core.utils.viewBinding
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.FragmentListTemplateBinding
import cz.cleevio.lightspeedskeleton.ui.listTemplateFragment.ListTemplateModel.Companion.mapItems
import lightbase.core.baseClasses.BaseFragment

class ListTemplateFragment : BaseFragment(R.layout.fragment_list_template) {

	private val binding by viewBinding(FragmentListTemplateBinding::bind)

	private val adapter: ListTemplateAdapter by lazy {
		ListTemplateAdapter {
			// Handle on click callback
		}
	}

	override fun initView() {
		binding.recycler.adapter = adapter

		val items = listOf(
			ListTemplateItem(id = 1, content = "Item 1"),
			ListTemplateItem(id = 2, content = "Item 2"),
			ListTemplateItem(id = 3, content = "Item 3"),
			ListTemplateItem(id = 4, content = "Item 4"),
			ListTemplateItem(id = 5, content = "Item 5")
		).mapItems()

		adapter.submitList(items)
	}

	override fun bindObservers() = Unit
}