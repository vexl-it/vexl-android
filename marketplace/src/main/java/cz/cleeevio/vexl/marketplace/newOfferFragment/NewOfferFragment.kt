package cz.cleeevio.vexl.marketplace.newOfferFragment

import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentNewOfferBinding
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.model.OfferTypeValue
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewOfferFragment : BaseFragment(R.layout.fragment_new_offer) {

	private val binding by viewBinding(FragmentNewOfferBinding::bind)
	override val viewModel by viewModel<NewOfferViewModel>()

	override fun bindObservers() {
	}

	override fun initView() {
		binding.newOfferBtn.setOnClickListener {
			val params = NewOfferParams(
				description = binding.newOfferDescription.text.toString(),
				location = binding.newOfferLocation.getLocationValue(),
				fee = binding.newOfferFee.getFeeValue(),
				priceRange = binding.newOfferRange.getPriceRangeValue(),
				priceTrigger = binding.newOfferPriceTrigger.getPriceTriggerValue(),
				paymentMethod = binding.newOfferPaymentMethod.getPaymentValue(),
				btcNetwork = binding.newOfferBtcNetwork.getBtcNetworkValue(),
				friendLevel = binding.newOfferFriendLevel.getFriendLevel(),
				offerType = OfferTypeValue(OfferType.BUY) // TODO get correct value
			)
			viewModel.createOffer(params)
		}
	}
}