package cz.cleeevio.vexl.marketplace.createOfferFragment

import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentCreateOfferBinding
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class CreateOfferFragment : BaseFragment(R.layout.fragment_create_offer) {

	private val binding by viewBinding(FragmentCreateOfferBinding::bind)
	override val viewModel by viewModel<CreateOfferViewModel>()

	override fun bindObservers() {

	}

	override fun initView() {

	}

}