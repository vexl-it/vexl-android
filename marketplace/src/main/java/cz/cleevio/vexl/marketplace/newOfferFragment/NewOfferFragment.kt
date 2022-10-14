package cz.cleevio.vexl.marketplace.newOfferFragment

import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.model.toUnixTimestamp
import cz.cleevio.core.utils.getBitmap
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.setDebouncedOnClickListener
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.Currency.Companion.mapStringToCurrency
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.dpValueToPx
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.marketplace.LocationSuggestionAdapter
import cz.cleevio.vexl.marketplace.R
import cz.cleevio.vexl.marketplace.SelectGroupAdapter
import cz.cleevio.vexl.marketplace.databinding.FragmentNewOfferBinding
import cz.cleevio.vexl.marketplace.editOfferFragment.NUMBER_OF_COLUMNS
import cz.cleevio.vexl.marketplace.encryptingProgressFragment.EncryptingProgressBottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewOfferFragment : BaseFragment(R.layout.fragment_new_offer) {

	private val binding by viewBinding(FragmentNewOfferBinding::bind)
	override val viewModel by viewModel<NewOfferViewModel>()

	private val args by navArgs<NewOfferFragmentArgs>()
	lateinit var adapter: SelectGroupAdapter

	override fun onResume() {
		super.onResume()

		if (viewModel.encryptedPreferenceRepository.isOfferEncrypted) {
			findNavController().popBackStack()
			viewModel.encryptedPreferenceRepository.isOfferEncrypted = false
		} else {
			showProgressIndicator(false)
		}
	}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.userFlow.collect {
				it?.let { user ->
					binding.newOfferFriendLevel.setUserAvatar(user.avatarBase64?.getBitmap() ?: user.avatar, user.anonymousAvatarImageIndex)
				}
			}
		}

		repeatScopeOnStart {
			viewModel.contactsPublicKeys.collect { (_, contactKeys) ->
				viewModel.fetchCommonFriends(contactKeys)
			}
		}

		repeatScopeOnStart {
			viewModel.contactKeysAndCommonFriends.collect { (params, contactKeys, commonFriends) ->
				viewModel.createOffer(params, contactKeys, commonFriends)
				binding.progress.isVisible = false
			}
		}

		repeatScopeOnStart {
			viewModel.showEncryptingDialogFlow.collect {
				showBottomDialog(EncryptingProgressBottomSheetDialog(it, isNewOffer = true) { resource ->
					when (resource.status) {
						is Status.Success -> {
							findNavController().popBackStack()
							viewModel.encryptedPreferenceRepository.isOfferEncrypted = false
						}
						is Status.Error -> {
							showProgressIndicator(false)
						}
						else -> Unit
					}
				})
			}
		}

		repeatScopeOnStart {
			viewModel.groups.collect { groups ->
				adapter.submitList(groups)
				binding.groupTitle.isVisible = groups.isNotEmpty()
				binding.groupDescription.isVisible = groups.isNotEmpty()
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
	}

	override fun initView() {
		binding.newOfferRange.setupWithCurrency(viewModel.encryptedPreferenceRepository.selectedCurrency.mapStringToCurrency())
		viewModel.loadMyContactsKeys()

		adapter = SelectGroupAdapter()
		val layoutManager = GridLayoutManager(requireContext(), NUMBER_OF_COLUMNS)
		binding.groupRecycler.layoutManager = layoutManager
		binding.groupRecycler.adapter = adapter

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
		binding.newOfferDescription.addTextChangedListener {
			binding.descriptionCounter.text = getString(
				R.string.widget_offer_description_counter, it?.length ?: 0, MAX_INPUT_LENGTH
			)
		}

		binding.newOfferLocation.setFragmentManager(parentFragmentManager)
		binding.newOfferDeleteTrigger.setFragmentManager(parentFragmentManager)

		binding.advancedBtn.setOnClickListener {
			viewModel.isAdvancedSectionShowed = !viewModel.isAdvancedSectionShowed
			if (viewModel.isAdvancedSectionShowed) {
				binding.advancedGroup.isVisible = true
				binding.advancedBtn.animate().setDuration(DURATION).rotation(START_ROTATION).start()
				Handler(Looper.getMainLooper()).postDelayed(DURATION) {
					binding.nestedScrollView.smoothScrollTo(
						binding.newOfferFriendLevel.x.toInt(),
						binding.newOfferFriendLevel.y.toInt() - Resources.getSystem().displayMetrics.heightPixels / 3
					)
				}
			} else {
				binding.advancedGroup.isVisible = false
				binding.advancedBtn.animate().setDuration(DURATION).rotation(MAX_ROTATION).start()
			}
		}

		binding.triggerBtn.setOnClickListener {
			viewModel.isTriggerSectionShowed = !viewModel.isTriggerSectionShowed
			if (viewModel.isTriggerSectionShowed) {
				binding.triggerGroup.isVisible = true
				binding.triggerBtn.animate().setDuration(DURATION).rotation(START_ROTATION).start()
				Handler(Looper.getMainLooper()).postDelayed(DURATION) {
					binding.nestedScrollView.smoothScrollTo(
						binding.newOfferDeleteTrigger.x.toInt(),
						binding.newOfferDeleteTrigger.y.toInt() - Resources.getSystem().displayMetrics.heightPixels / DISPLAY_THIRD
					)
				}
			} else {
				binding.triggerGroup.isVisible = false
				binding.triggerBtn.animate().setDuration(DURATION).rotation(MAX_ROTATION).start()
			}
		}

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

		binding.newOfferLocation.setupOnTextChanged { query, view ->
			viewModel.getSuggestions(query, view)
		}

		binding.newOfferCurrency.onCurrencyPicked = {
			binding.newOfferRange.setupWithCurrency(it)
		}

		binding.newOfferBtn.text = when (args.offerType) {
			OfferType.BUY -> getString(R.string.offer_create_buy_btn)
			OfferType.SELL -> getString(R.string.offer_create_sell_btn)
		}
		binding.newOfferBtn.setDebouncedOnClickListener {
			val params = viewModel.offerUtils.isOfferParamsValid(
				activity = requireActivity(),
				description = binding.newOfferDescription.text.toString(),
				location = binding.newOfferLocation.getLocationValue(),
				fee = binding.newOfferFee.getFeeValue(),
				priceRange = binding.newOfferRange.getPriceRangeValue(),
				friendLevel = binding.newOfferFriendLevel.getSingleChoiceFriendLevelValue(),
				priceTrigger = binding.newOfferPriceTrigger.getPriceTriggerValue(),
				btcNetwork = binding.newOfferBtcNetwork.getBtcNetworkValue(),
				paymentMethod = binding.newOfferPaymentMethod.getPaymentValue(),
				offerType = args.offerType.name,
				expiration = binding.newOfferDeleteTrigger.getValue().toUnixTimestamp(),
				active = true,
				currency = binding.newOfferRange.getCurrencyName(),
				groupUuids = adapter.getSelectedGroupUuids()
			)

			if (params != null) {
				showProgressIndicator(true)
				viewModel.fetchContactsPublicKeys(params)
			}
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME
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
		private const val DURATION = 300L
		private const val MAX_ROTATION = 180f
		private const val START_ROTATION = 0f
	}
}
