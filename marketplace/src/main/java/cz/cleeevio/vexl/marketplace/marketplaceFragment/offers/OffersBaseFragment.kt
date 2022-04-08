package cz.cleeevio.vexl.marketplace.marketplaceFragment.offers

import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentOffersBinding
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment

sealed class OffersBaseFragment : BaseFragment(R.layout.fragment_offers) {

	val binding by viewBinding(FragmentOffersBinding::bind)

}