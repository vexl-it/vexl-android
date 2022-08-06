package cz.cleevio.vexl.marketplace.newOfferFragment

import android.content.res.Resources
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.model.toUnixTimestamp
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.FriendLevel
import cz.cleevio.network.data.Status
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.dpValueToPx
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.marketplace.LocationSuggestionAdapter
import cz.cleevio.vexl.marketplace.R
import cz.cleevio.vexl.marketplace.databinding.FragmentNewOfferBinding
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
					is Status.Error -> {
						binding.newOfferBtn.isVisible = true
						binding.progress.isVisible = false
					}
				}
			}
		}

		repeatScopeOnStart {
			viewModel.suggestions.collect { (offerLocationItem, queries) ->
				if (queries.isEmpty()) return@collect
				if (queries.map { it.city }.contains(offerLocationItem?.getEditText()?.text.toString()))
					return@collect

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
		viewModel.loadMyContactsKeys()

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

		binding.newOfferLocation.setupFocusChangeListener { hasFocus, y ->
			if (hasFocus) {
				binding.nestedScrollView.smoothScrollTo(
					binding.newOfferLocation.x.toInt(),
					y + binding.newOfferLocation.y.toInt() - Resources.getSystem().displayMetrics.heightPixels / DISPLAY_THIRD
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
		binding.newOfferBtn.setOnClickListener {
			val description = binding.newOfferDescription.text.toString()
			if (description.isBlank()) {
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
				priceRange = binding.newOfferRange.getPriceRangeValue(),
				priceTrigger = priceTrigger,
				paymentMethod = paymentMethod,
				btcNetwork = btcNetwork,
				friendLevel = friendLevel,
				offerType = args.offerType.name,
				expiration = expiration,
				active = true,
				currency = binding.newOfferRange.currentCurrency.name
			)
			binding.newOfferBtn.isVisible = false
			binding.progress.isVisible = true
			viewModel.createOffer(params)
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME
			)
		}
	}

	companion object {
		const val MAX_INPUT_LENGTH = 140
		private const val DISPLAY_THIRD = 3
		private const val SUGGESTION_PADDING = 8
	}
}
