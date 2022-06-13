package cz.cleeevio.vexl.marketplace.filtersFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentFiltersBinding
import cz.cleevio.core.utils.viewBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets

class FiltersFragment : BaseFragment(R.layout.fragment_filters) {

	private val binding by viewBinding(FragmentFiltersBinding::bind)
	private val args by navArgs<FiltersFragmentArgs>()

	override fun bindObservers() {
	}

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME
			)
		}

		binding.filterTitleWidget.setTypeAndTitle(args.offerType, getString(R.string.filter_title))
		binding.filterTitleWidget.setListeners(
			onReset = {
				//reset all fields
				resetAllInputs()
			},
			onClose = {
				//close this screen
				findNavController().popBackStack()
			}
		)

		binding.applyBtn.setOnClickListener {
			//TODO: somehow apply filters? Save to DB?

			//and go back to offers
			findNavController().popBackStack()
		}

		binding.filterLocation.setFragmentManager(parentFragmentManager)
	}

	private fun resetAllInputs() {
		binding.priceRangeWidget.reset()
		binding.filterOfferFee.reset()
		binding.filterLocation.reset()
		binding.paymentMethod.reset()
		binding.networkType.reset()
		binding.friendLevel.reset()
	}

}