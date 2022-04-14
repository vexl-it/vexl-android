package cz.cleeevio.vexl.marketplace.newOfferFragment

import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentNewOfferBinding
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewOfferFragment : BaseFragment(R.layout.fragment_new_offer) {

	private val binding by viewBinding(FragmentNewOfferBinding::bind)
	override val viewModel by viewModel<NewOfferViewModel>()

	override fun bindObservers() {
	}

	override fun initView() {
	}

}