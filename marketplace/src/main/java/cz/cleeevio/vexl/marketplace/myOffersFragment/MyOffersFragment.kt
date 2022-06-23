package cz.cleeevio.vexl.marketplace.myOffersFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentMyOffersBinding
import cz.cleeevio.vexl.marketplace.marketplaceFragment.offers.OffersAdapter
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.OfferWidget
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MyOffersFragment : BaseFragment(R.layout.fragment_my_offers) {

	private val binding by viewBinding(FragmentMyOffersBinding::bind)
	override val viewModel by viewModel<MyOffersViewModel> { parametersOf(args.offerType) }

	private val args by navArgs<MyOffersFragmentArgs>()

	lateinit var adapter: OffersAdapter
	private val onInteractWithOffer: (String) -> Unit = { offerId ->
		findNavController().navigate(
			MyOffersFragmentDirections.proceedToEditOfferFragment(offerId)
		)
	}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.offers.collect { offers ->
				adapter.submitList(offers)
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}

		adapter = OffersAdapter(onInteractWithOffer, OfferWidget.Mode.MY_OFFER)
		binding.myOffersList.adapter = adapter

		binding.newOfferTitle.setTypeAndTitle(
			when (args.offerType) {
				OfferType.BUY -> getString(R.string.offer_edit_buy_title)
				OfferType.SELL -> getString(R.string.offer_edit_sell_title)
			}
		)

		binding.myOffersNew.text = when (args.offerType) {
			OfferType.BUY -> getString(R.string.offer_create_buy_title)
			OfferType.SELL -> getString(R.string.offer_create_sell_title)
		}
		binding.myOffersNew.setOnClickListener {
			findNavController().navigate(
				MyOffersFragmentDirections.proceedToNewOfferFragment(args.offerType)
			)
		}

		binding.newOfferTitle.setListeners(
			onClose = {
				//close this screen
				findNavController().popBackStack()
			}
		)
	}
}