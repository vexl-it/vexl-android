package cz.cleevio.vexl.marketplace.editOfferFragment

import android.content.res.Resources
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import cz.cleevio.core.model.FeeValue
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.model.PriceTriggerValue
import cz.cleevio.core.model.toUnixTimestamp
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.*
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.Currency
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.model.offer.PriceTriggerType
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.dpValueToPx
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.extensions.showSnackbar
import cz.cleevio.vexl.marketplace.LocationSuggestionAdapter
import cz.cleevio.vexl.marketplace.R
import cz.cleevio.vexl.marketplace.SelectGroupAdapter
import cz.cleevio.vexl.marketplace.databinding.FragmentEditOfferBinding
import cz.cleevio.vexl.marketplace.encryptingProgressFragment.EncryptingProgressBottomSheetDialog
import cz.cleevio.vexl.marketplace.newOfferFragment.NewOfferFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val NUMBER_OF_COLUMNS = 2

class EditOfferFragment : BaseFragment(R.layout.fragment_edit_offer) {

	private val binding by viewBinding(FragmentEditOfferBinding::bind)
	override val viewModel by viewModel<EditOfferViewModel>()

	private val args by navArgs<EditOfferFragmentArgs>()
	lateinit var adapter: SelectGroupAdapter

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.userFlow.collect {
				it?.let { user ->
					binding.newOfferFriendLevel.setUserAvatar(user.avatar, user.anonymousAvatarImageIndex)
				}
			}
		}
		repeatScopeOnStart {
			viewModel.errorFlow.collect { resource ->
				if (resource.status is Status.Error) {
					showProgressIndicator(false)
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
			// Fixme: Update visibility logic once the functionality will be reviewed after conference
			viewModel.groups.collect { groups ->
				adapter.submitList(groups)
				binding.groupTitle.isVisible = false //groups.isNotEmpty()
				binding.groupDescription.isVisible = false //groups.isNotEmpty()
				binding.groupRecycler.isVisible = false
			}
		}

		repeatScopeOnStart {
			viewModel.offer.collect {
				it?.let { offer ->
					setupOffer(offer)
				}
			}
		}

		repeatScopeOnStart {
			viewModel.suggestions.collect { (offerLocationItem, queries) ->
				if (queries.isEmpty()) return@collect
				if (queries.map { it.cityText }.contains(offerLocationItem?.getEditText()?.text.toString())) {
					offerLocationItem?.setLocation(queries.first())
					return@collect
				}

				offerLocationItem?.getEditText()?.let {
					it.setAdapter(null)
					val adapter = LocationSuggestionAdapter(queries, requireActivity())

					it.dropDownVerticalOffset = requireContext().dpValueToPx(SUGGESTION_PADDING).toInt()
					it.setDropDownBackgroundResource(R.drawable.background_rounded)
					it.setAdapter(adapter)
					it.showDropDown()
					it.setOnItemClickListener { _, _, position, _ ->
						offerLocationItem.setLocation(queries[position])
					}
				}
			}
		}

		repeatScopeOnStart {
			viewModel.queryForSuggestions.collect { (view, query) ->
				view?.let {
					viewModel.getDebouncedSuggestions(query, it)
				}
			}
		}

		repeatScopeOnStart {
			viewModel.showEncryptingDialog.collect {
				showBottomDialog(EncryptingProgressBottomSheetDialog(it, isNewOffer = false) { resource ->
					if (resource.isSuccess()) {
						findNavController().popBackStack()
					} else {
						showProgressIndicator(false)
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
				})
			}
		}
	}

	private fun setupOffer(offer: Offer) {
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
				showProgressIndicator(true)
				viewModel.deleteOffer(
					offerId = offer.offerId,
					onSuccess = {
						findNavController().popBackStack()
					}
				)
			},
			onChangeActiveState = {
				updateOffer(!offer.active)
			}
		)

		binding.newOfferDescription.setText(offer.offerDescription)
		binding.amountRange.setupWithCurrency(Currency.valueOf(offer.currency))
		binding.amountRange.setValues(offer.amountBottomLimit.toFloat(), offer.amountTopLimit.toFloat())

		binding.newOfferFee.setValues(
			FeeValue(
				type = FeeButtonSelected.valueOf(offer.feeState),
				value = offer.feeAmount.toFloat()
			)
		)
		binding.newOfferLocation.setValues(offer.location, LocationButtonSelected.valueOf(offer.locationState))
		binding.newOfferPaymentMethod.setValues(offer.paymentMethod.map { method ->
			PaymentButtonSelected.valueOf(method.uppercase())
		})

		binding.newOfferCurrency.selectCurrencyManually(Currency.valueOf(offer.currency))

		binding.newOfferPriceTrigger.setPriceTriggerValue(
			PriceTriggerValue(
				value = offer.activePriceValue,
				type = PriceTriggerType.valueOf(offer.activePriceState),
				currency = offer.activePriceCurrency
			)
		)

		binding.newOfferBtcNetwork.setValues(offer.btcNetwork.map { btcNetwork ->
			BtcNetworkButtonSelected.valueOf(btcNetwork)
		})
		binding.newOfferFriendLevel.setValues(setOf(FriendLevel.valueOf(offer.friendLevel)))

		binding.newOfferBtn.text = when (OfferType.valueOf(offer.offerType)) {
			OfferType.BUY -> getString(R.string.offer_update_buy_btn)
			OfferType.SELL -> getString(R.string.offer_update_sell_btn)
		}
	}

	override fun initView() {
		viewModel.loadMyContactsKeys()

		adapter = SelectGroupAdapter()
		val layoutManager = GridLayoutManager(requireContext(), NUMBER_OF_COLUMNS)
		binding.groupRecycler.layoutManager = layoutManager
		binding.groupRecycler.adapter = adapter

		viewModel.loadOfferFromCacheById(args.offerId)

		binding.newOfferTitle.setListeners(
			onClose = {
				//close this screen
				findNavController().popBackStack()
			}
		)

		binding.newOfferPriceTrigger.onFocusChangeListener = { hasFocus ->
			if (hasFocus) {
				binding.nestedScrollView.smoothScrollTo(
					binding.newOfferPriceTrigger.x.toInt(),
					binding.newOfferPriceTrigger.y.toInt() - Resources.getSystem().displayMetrics.heightPixels / DISPLAY_THIRD
				)
			}
		}

		binding.newOfferDeleteTrigger.onFocusChangeListener = { hasFocus ->
			if (hasFocus) {
				binding.nestedScrollView.smoothScrollTo(
					binding.newOfferDeleteTrigger.x.toInt(),
					binding.newOfferDeleteTrigger.y.toInt() - Resources.getSystem().displayMetrics.heightPixels / DISPLAY_THIRD
				)
			}
		}

		binding.newOfferLocation.setupFocusChangeListener { hasFocus, locationItem ->
			if (hasFocus) {
				binding.nestedScrollView.smoothScrollTo(
					binding.newOfferLocation.x.toInt(),
					locationItem.height *
						binding.newOfferLocation.getPositionOfItem(locationItem) +
						requireContext().dpValueToPx(OFFER_ITEM_PADDING).toInt() +
						binding.newOfferLocation.y.toInt() -
						Resources.getSystem().displayMetrics.heightPixels / DISPLAY_THIRD
				)
			}
		}

		binding.newOfferCurrency.onCurrencyPicked = {
			binding.amountRange.setupWithCurrency(it)
		}

		binding.newOfferLocation.setupOnTextChanged { query, view ->
			viewModel.getSuggestions(query, view)
		}

		binding.descriptionCounter.text = getString(R.string.widget_offer_description_counter, 0, MAX_INPUT_LENGTH)
		binding.newOfferDescription.addTextChangedListener {
			binding.descriptionCounter.text = getString(
				R.string.widget_offer_description_counter, it?.length ?: 0, NewOfferFragment.MAX_INPUT_LENGTH
			)
		}

		binding.newOfferLocation.setFragmentManager(parentFragmentManager)
		binding.newOfferDeleteTrigger.setFragmentManager(parentFragmentManager)

		binding.newOfferBtn.setOnClickListener {
			updateOffer(true)
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME
			)
		}
	}

	private fun updateOffer(active: Boolean) {
		val params = OfferUtils.isOfferParamsValid(
			activity = requireActivity(),
			description = binding.newOfferDescription.text.toString(),
			location = binding.newOfferLocation.getLocationValue(),
			fee = binding.newOfferFee.getFeeValue(),
			priceRange = binding.amountRange.getPriceRangeValue(),
			friendLevel = binding.newOfferFriendLevel.getSingleChoiceFriendLevelValue(),
			priceTrigger = binding.newOfferPriceTrigger.getPriceTriggerValue(),
			btcNetwork = binding.newOfferBtcNetwork.getBtcNetworkValue(),
			paymentMethod = binding.newOfferPaymentMethod.getPaymentValue(),
			offerType = viewModel.offer.value?.offerType ?: return,
			expiration = binding.newOfferDeleteTrigger.getValue().toUnixTimestamp(),
			active = active,
			currency = binding.amountRange.currentCurrency.name,
			groupUuids = adapter.getSelectedGroupUuids()
		)

		if (params != null) {
			viewModel.updateOffer(
				offerId = args.offerId,
				params = params
			)
		}
	}

	private fun showProgressIndicator(isVisible: Boolean) {
		binding.newOfferBtn.isVisible = !isVisible
		binding.progress.isVisible = isVisible
	}

	companion object {
		const val MAX_INPUT_LENGTH = 140
		private const val DISPLAY_THIRD = 3
		private const val SUGGESTION_PADDING = 8
		private const val OFFER_ITEM_PADDING = 32
	}
}
