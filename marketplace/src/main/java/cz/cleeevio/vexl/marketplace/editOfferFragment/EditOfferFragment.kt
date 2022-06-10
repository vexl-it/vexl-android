package cz.cleeevio.vexl.marketplace.editOfferFragment

import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentEditOfferBinding
import cz.cleevio.core.model.FeeValue
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.*
import cz.cleevio.network.data.Status
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import lightbase.core.extensions.showSnackbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class EditOfferFragment : BaseFragment(R.layout.fragment_edit_offer) {

	private val binding by viewBinding(FragmentEditOfferBinding::bind)
	override val viewModel by viewModel<EditOfferViewModel>()

	private val args by navArgs<EditOfferFragmentArgs>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.updateOfferRequest.collect { resource ->
				when (resource.status) {
					is Status.Success -> {
						findNavController().popBackStack()
					}
					is Status.Error -> {
						//show error toast
						resource.errorIdentification.message?.let { messageCode ->
							if (messageCode != -1) {
								showSnackbar(
									view = binding.container,
									message = getString(messageCode)
								)
							}
						}
					}
				}
			}
		}

		repeatScopeOnStart {
			viewModel.offer.collect {
				it?.let { offer ->
					//todo: set values from offer
					binding.newOfferTitle.setTypeAndTitle(
						when (OfferType.valueOf(offer.offerType)) {
							OfferType.BUY -> getString(R.string.offer_create_buy_title)
							OfferType.SELL -> getString(R.string.offer_create_sell_title)
						}
					)

					//todo: add offer state widget

					binding.descriptionTitle.text = offer.offerDescription
					binding.amountRange.setValues(offer.amountBottomLimit.toFloat(), offer.amountTopLimit.toFloat())
					binding.newOfferFee.setValues(
						FeeValue(
							type = FeeButtonSelected.valueOf(offer.feeState),
							value = offer.feeAmount.intValueExact()
						)
					)
					binding.newOfferLocation.setValues(offer.location, LocationButtonSelected.valueOf(offer.locationState))
					binding.newOfferPaymentMethod.setValues(offer.paymentMethod.map { method ->
						PaymentButtonSelected.valueOf(method)
					})
					//todo: we need BE to support price trigger
					//todo: we need BE to support time delete trigger

					binding.newOfferBtcNetwork.setValues(offer.btcNetwork.map { btcNetwork ->
						BtcNetworkButtonSelected.valueOf(btcNetwork)
					})
					binding.newOfferFriendLevel.setValues(FriendLevel.valueOf(offer.friendLevel))
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

		viewModel.loadOfferById(args.offerId)

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
			val params = OfferParams(
				description = binding.newOfferDescription.text.toString(),
				location = binding.newOfferLocation.getLocationValue(),
				fee = binding.newOfferFee.getFeeValue(),
				priceRange = binding.amountRange.getPriceRangeValue(),
				priceTrigger = binding.newOfferPriceTrigger.getPriceTriggerValue(),
				paymentMethod = binding.newOfferPaymentMethod.getPaymentValue(),
				btcNetwork = binding.newOfferBtcNetwork.getBtcNetworkValue(),
				friendLevel = binding.newOfferFriendLevel.getFriendLevel(),
				offerType = viewModel.offer.value!!.offerType
			)
			viewModel.updateOffer(offerId = args.offerId, params = params)
		}
	}

	companion object {
		const val MAX_INPUT_LENGTH = 140
	}
}