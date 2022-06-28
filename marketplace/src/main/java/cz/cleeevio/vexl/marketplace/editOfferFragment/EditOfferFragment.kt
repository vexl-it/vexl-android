package cz.cleeevio.vexl.marketplace.editOfferFragment

import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentEditOfferBinding
import cz.cleeevio.vexl.marketplace.newOfferFragment.NewOfferFragment
import cz.cleevio.core.model.*
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.*
import cz.cleevio.network.data.Status
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import lightbase.core.extensions.showSnackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class EditOfferFragment : BaseFragment(R.layout.fragment_edit_offer) {

	private val binding by viewBinding(FragmentEditOfferBinding::bind)
	override val viewModel by viewModel<EditOfferViewModel>()

	private val args by navArgs<EditOfferFragmentArgs>()

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.errorFlow.collect { resource ->
				if (resource.status is Status.Error) {
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

		repeatScopeOnStart {
			viewModel.offer.collect {
				it?.let { offer ->
					Timber.tag("OFFER").d("$offer")
					binding.newOfferTitle.setTypeAndTitle(
						when (OfferType.valueOf(offer.offerType)) {
							OfferType.BUY -> getString(R.string.offer_edit_buy_title)
							OfferType.SELL -> getString(R.string.offer_edit_sell_title)
						}
					)

					binding.offerState.setActive(offer.active)
					binding.offerState.setListeners(
						onDelete = {
							viewModel.deleteOffer(offer.offerId, onSuccess = {
								findNavController().popBackStack()
							})
						},
						onChangeActiveState = {
							val params = OfferParams(
								description = binding.newOfferDescription.text.toString(),
								location = binding.newOfferLocation.getLocationValue(),
								fee = binding.newOfferFee.getFeeValue(),
								priceRange = binding.amountRange.getPriceRangeValue(),
								priceTrigger = binding.newOfferPriceTrigger.getPriceTriggerValue(),
								paymentMethod = binding.newOfferPaymentMethod.getPaymentValue(),
								btcNetwork = binding.newOfferBtcNetwork.getBtcNetworkValue(),
								friendLevel = binding.newOfferFriendLevel.getFriendLevel(),
								offerType = offer.offerType,
								expiration = binding.newOfferDeleteTrigger.getValue().toUnixTimestamp(),
								active = !offer.active
							)
							viewModel.updateOffer(offerId = offer.offerId, params = params, onSuccess = {
								findNavController().popBackStack()
							})
						}
					)

					binding.newOfferDescription.setText(offer.offerDescription)
					binding.amountRange.setValues(offer.amountBottomLimit.toFloat(), offer.amountTopLimit.toFloat())
					binding.newOfferFee.setValues(
						FeeValue(
							type = FeeButtonSelected.valueOf(offer.feeState),
							value = offer.feeAmount.toFloat()
						)
					)
					binding.newOfferLocation.setValues(offer.location, LocationButtonSelected.valueOf(offer.locationState))
					binding.newOfferPaymentMethod.setValues(offer.paymentMethod.map { method ->
						PaymentButtonSelected.valueOf(method)
					})

					binding.newOfferPriceTrigger.setPriceTriggerValue(
						PriceTriggerValue(
							value = offer.activePriceValue,
							type = TriggerType.valueOf(offer.activePriceState)
						)
					)
					//todo: we need someone to specify behavior for time delete trigger

					binding.newOfferBtcNetwork.setValues(offer.btcNetwork.map { btcNetwork ->
						BtcNetworkButtonSelected.valueOf(btcNetwork)
					})
					binding.newOfferFriendLevel.setValues(FriendLevel.valueOf(offer.friendLevel))

					binding.newOfferBtn.text = when (OfferType.valueOf(offer.offerType)) {
						OfferType.BUY -> getString(R.string.offer_update_buy_btn)
						OfferType.SELL -> getString(R.string.offer_update_sell_btn)
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

		viewModel.loadOfferFromCacheById(args.offerId)

		binding.newOfferTitle.setListeners(
			onClose = {
				//close this screen
				findNavController().popBackStack()
			}
		)

		binding.descriptionCounter.text = getString(R.string.widget_offer_description_counter, 0, MAX_INPUT_LENGTH)
		binding.newOfferDescription.addTextChangedListener {
			binding.descriptionCounter.text = getString(
				R.string.widget_offer_description_counter, it?.length ?: 0, NewOfferFragment.MAX_INPUT_LENGTH
			)
		}

		binding.newOfferLocation.setFragmentManager(parentFragmentManager)
		binding.newOfferDeleteTrigger.setFragmentManager(parentFragmentManager)

		binding.newOfferBtn.setOnClickListener {
			val description = binding.newOfferDescription.text.toString()
			if (description.isNullOrBlank()) {
				Toast.makeText(requireActivity(), "Missing description", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			val location = binding.newOfferLocation.getLocationValue()
			if (location.values.isEmpty()) {
				Toast.makeText(requireActivity(), "Missing location", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			val paymentMethod = binding.newOfferPaymentMethod.getPaymentValue()
			if (paymentMethod.value.isEmpty()) {
				Toast.makeText(requireActivity(), "Missing payment method", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			val priceTrigger = binding.newOfferPriceTrigger.getPriceTriggerValue()
			if (priceTrigger.value == null) {
				Toast.makeText(requireActivity(), "Invalid price trigger", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			val btcNetwork = binding.newOfferBtcNetwork.getBtcNetworkValue()
			if (btcNetwork.value.isEmpty()) {
				Toast.makeText(requireActivity(), "Invalid type", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			val friendLevel = binding.newOfferFriendLevel.getFriendLevel()
			if (friendLevel.value == FriendLevel.NONE) {
				Toast.makeText(requireActivity(), "Invalid friend level", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			val expiration = binding.newOfferDeleteTrigger.getValue().toUnixTimestamp()
			if (expiration < System.currentTimeMillis()) {
				Toast.makeText(requireActivity(), "Invalid delete trigger", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			val params = OfferParams(
				description = description,
				location = location,
				fee = binding.newOfferFee.getFeeValue(),
				priceRange = binding.amountRange.getPriceRangeValue(),
				priceTrigger = priceTrigger,
				paymentMethod = paymentMethod,
				btcNetwork = btcNetwork,
				friendLevel = friendLevel,
				offerType = viewModel.offer.value!!.offerType,
				expiration = expiration,
				active = viewModel.offer.value!!.active
			)
			viewModel.updateOffer(offerId = args.offerId, params = params, onSuccess = {
				findNavController().popBackStack()
			})
		}
	}

	companion object {
		const val MAX_INPUT_LENGTH = 140
	}
}