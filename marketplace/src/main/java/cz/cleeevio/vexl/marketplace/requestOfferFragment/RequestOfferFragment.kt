package cz.cleeevio.vexl.marketplace.requestOfferFragment

import androidx.navigation.fragment.navArgs
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentRequestOfferBinding
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment

class RequestOfferFragment : BaseFragment(R.layout.fragment_request_offer) {

	private val args by navArgs<RequestOfferFragmentArgs>()
	private val binding by viewBinding(FragmentRequestOfferBinding::bind)

	override fun bindObservers() {
	}

	override fun initView() {
		binding.offerId.text = "Requesting offer with id ${args.offerId}"
	}

}