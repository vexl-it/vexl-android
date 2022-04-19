package cz.cleeevio.vexl.marketplace.filtersFragment

import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentFiltersBinding
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment

class FiltersFragment : BaseFragment(R.layout.fragment_filters) {

	private val binding by viewBinding(FragmentFiltersBinding::bind)

	override fun bindObservers() {
	}

	override fun initView() {
		binding.priceTriggerWidget.setupData(2.0f) //TODO setup real price of the cryptocurrency
	}

}