package cz.cleeevio.vexl.marketplace.myOffersFragment

import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentMyOffersBinding
import cz.cleeevio.vexl.marketplace.marketplaceFragment.offers.OffersAdapter
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyOffersFragment : BaseFragment(R.layout.fragment_my_offers) {

	private val binding by viewBinding(FragmentMyOffersBinding::bind)
	override val viewModel by viewModel<MyOffersViewModel>()

	private val args by navArgs<MyOffersFragmentArgs>()

	lateinit var adapter: OffersAdapter
	val onInteractWithOffer: (String) -> Unit = {
		//find directions, go to...
	}

	override fun bindObservers() {

	}

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}

		adapter = OffersAdapter(onInteractWithOffer)
		binding.offerList.adapter = adapter

		binding.newOfferTitle.setTypeAndTitle(
			when (args.offerType) {
				OfferType.BUY -> getString(R.string.offer_edit_buy_title)
				OfferType.SELL -> getString(R.string.offer_edit_sell_title)
			}
		)

		binding.newOfferTitle.setListeners(
			onClose = {
				//close this screen
				findNavController().popBackStack()
			}
		)


	}
}