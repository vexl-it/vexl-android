package cz.cleevio.vexl.marketplace.myOffersFragment

import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.ChipViewUtils
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.safeNavigateWithTransition
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.OfferWidget
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.marketplace.R
import cz.cleevio.vexl.marketplace.databinding.FragmentMyOffersBinding
import cz.cleevio.vexl.marketplace.marketplaceFragment.offers.OffersAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MyOffersFragment : BaseFragment(R.layout.fragment_my_offers) {

	private val binding by viewBinding(FragmentMyOffersBinding::bind)
	override val viewModel by viewModel<MyOffersViewModel> { parametersOf(args.offerType) }

	private val args by navArgs<MyOffersFragmentArgs>()

	lateinit var adapter: OffersAdapter
	private val onInteractWithOffer: (String) -> Unit = { offerId ->
		findNavController().safeNavigateWithTransition(
			MyOffersFragmentDirections.proceedToEditOfferFragment(offerId)
		)
	}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.offers.collect { offers ->
				adapter.submitList(offers)
			}
		}

		repeatScopeOnStart {
			viewModel.offersCount.collect { myOffersCount ->
				binding.myOffersActive.text = getString(R.string.offer_active_count, myOffersCount)
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

		binding.myOffersSort.addView(ChipViewUtils.generateChipView(
			context = requireContext(),
			icon = R.drawable.ic_chevron_down,
			iconAtStart = false,
			filter = getString(R.string.offer_sort_newest),
			listener = {
				Toast.makeText(requireActivity(), "Sort not implemented yet", Toast.LENGTH_SHORT).show()
			}
		))

		adapter = OffersAdapter(onInteractWithOffer, OfferWidget.Mode.MY_OFFER)
		binding.myOffersList.adapter = adapter

		binding.newOfferTitle.setTypeAndTitle(
			when (args.offerType) {
				OfferType.BUY -> getString(R.string.offer_edit_buy_title)
				OfferType.SELL -> getString(R.string.offer_edit_sell_title)
			}
		)

		binding.addButtonText.text = when (args.offerType) {
			OfferType.BUY -> getString(R.string.offer_create_buy_title)
			OfferType.SELL -> getString(R.string.offer_create_sell_title)
		}
		binding.myOffersNew.setOnClickListener {
			findNavController().safeNavigateWithTransition(
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

	override fun onResume() {
		super.onResume()

		viewModel.loadData()
	}
}
