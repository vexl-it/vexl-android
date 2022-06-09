package cz.cleeevio.vexl.marketplace.newOfferFragment

import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentNewOfferBinding
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.network.data.Status
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewOfferFragment : BaseFragment(R.layout.fragment_new_offer) {

	private val binding by viewBinding(FragmentNewOfferBinding::bind)
	override val viewModel by viewModel<NewOfferViewModel>()

	private val args by navArgs<NewOfferFragmentArgs>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.newOfferRequest.collect { resource ->
				when (resource.status) {
					is Status.Success -> {
						findNavController().popBackStack()
					}
				}
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME
			)
		}

		binding.newOfferTitle.setTypeAndTitle(
			when (args.offerType) {
				OfferType.BUY -> getString(R.string.offer_create_buy_title)
				OfferType.SELL -> getString(R.string.offer_create_sell_title)
			}
		)
		binding.newOfferTitle.setListeners(
			onClose = {
				//close this screen
				findNavController().popBackStack()
			}
		)

		binding.descriptionCounter.text = getString(R.string.widget_offer_description_counter, 0, MAX_INPUT_LENGTH)
		binding.newOfferDescription.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(s: Editable?) {
			}

			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
			}

			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
				binding.descriptionCounter.text = getString(R.string.widget_offer_description_counter, count, MAX_INPUT_LENGTH)
			}
		})

		binding.newOfferLocation.setFragmentManager(parentFragmentManager)
		binding.newOfferDeleteTrigger.setFragmentManager(parentFragmentManager)

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
				offerType = args.offerType.name
			)
			viewModel.createOffer(params)
		}
	}

	companion object {
		const val MAX_INPUT_LENGTH = 140
	}
}