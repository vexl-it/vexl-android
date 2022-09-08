package cz.cleevio.vexl.marketplace.requestOfferFragment

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForIMEInset
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.marketplace.R
import cz.cleevio.vexl.marketplace.databinding.FragmentRequestOfferBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class RequestOfferFragment : BaseFragment(R.layout.fragment_request_offer) {

	override val viewModel by viewModel<RequestOfferViewModel>()

	private val args by navArgs<RequestOfferFragmentArgs>()
	private val binding by viewBinding(FragmentRequestOfferBinding::bind)

	private lateinit var commonFriendsAdapter: CommonFriendAdapter

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.isRequesting.collect { isRequesting ->
				if (isRequesting) {
					showRequesting()
				} else {
					hideRequesting()
				}
			}
		}

		repeatScopeOnStart {
			viewModel.offer.collect {
				it?.let { offerWithGroup ->
					Timber.tag("ASDX").d("${offerWithGroup.offer}")
					val contacts = offerWithGroup.offer.commonFriends.map { friend -> friend.contact }
					binding.commonFriendsPlaceholder.isVisible = contacts.isNotEmpty()
					binding.commonFriendsEmpty.isVisible = contacts.isEmpty()
					binding.offerWidget.bind(item = offerWithGroup.offer, group = offerWithGroup.group)

					if (contacts.isNotEmpty()) {
						binding.commonFriendsNumber.text = getString(R.string.request_common_friends, offerWithGroup.offer.commonFriends.size)
						commonFriendsAdapter.submitList(offerWithGroup.offer.commonFriends.map { friend -> friend.contact })
					}
				}
			}
		}
	}

	override fun initView() {
		commonFriendsAdapter = CommonFriendAdapter()
		binding.commonFriendsList.adapter = commonFriendsAdapter

		viewModel.loadOfferById(args.offerId)

		binding.requestOfferBtn.setOnClickListener {
			val offerPublicKey = viewModel.offer.value?.offer?.offerPublicKey
			val offerId = viewModel.offer.value?.offer?.offerId
			val messageText = binding.requestText.text.toString()

			if (offerPublicKey.isNullOrBlank() || offerId.isNullOrBlank()) {
				if (offerPublicKey.isNullOrBlank()) {
					viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
						Toast.makeText(
							requireActivity(),
							getString(R.string.error_missing_offer_public_key),
							Toast.LENGTH_SHORT
						).show()
					}
				}
				if (offerId.isNullOrBlank()) {
					viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
						Toast.makeText(
							requireActivity(),
							getString(R.string.error_missing_offer_id),
							Toast.LENGTH_SHORT
						).show()
					}
				}
			} else {
				viewModel.sendRequest(
					text = messageText.ifBlank { null },
					offerPublicKey = offerPublicKey,
					offerId = offerId
				) {
					viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
						Toast.makeText(
							requireContext(),
							getString(R.string.offer_request_sent_successfully),
							Toast.LENGTH_SHORT
						).show()
						delay(POP_BACKSTACK_DELAY)
						findNavController().popBackStack()
					}
				}
			}
		}

		binding.close.setOnClickListener {
			findNavController().popBackStack()
		}

		listenForInsets(binding.requestOfferBtn) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}

		val defaultButtonMargin = binding.requestOfferBtn.marginBottom
		listenForIMEInset(binding.container) { bottomInset ->
			binding.requestOfferBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				bottomMargin = bottomInset + defaultButtonMargin
				topMargin = bottomInset + defaultButtonMargin
			}
		}

		binding.requestText.setOnFocusChangeListener { _, hasFocus ->
			if (hasFocus) {
				binding.container.postDelayed({
					if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
						binding.nestedScrollView.fullScroll(View.FOCUS_DOWN)
						binding.requestText.requestFocus()
					}
				}, SCROLL_TO_BOTTOM_DELAY)
			}
		}

		val buttonMargin = binding.requestOfferBtn.marginBottom
		listenForIMEInset(binding.container)
		{ insets ->
			binding.requestOfferBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				bottomMargin = insets + buttonMargin
			}
		}
	}

	private fun showRequesting() {
		binding.offerTitle.text = getString(R.string.request_title_loading)
		binding.commonFriendsPlaceholder.isVisible = false
		binding.requestTextWrapper.isVisible = false
		binding.requestOfferBtn.isVisible = false
		binding.close.isVisible = false
	}

	private fun hideRequesting() {
		binding.offerTitle.text = getString(R.string.request_title)
		binding.commonFriendsPlaceholder.isVisible = true
		binding.requestTextWrapper.isVisible = true
		binding.requestOfferBtn.isVisible = true
		binding.close.isVisible = true
	}

	private companion object {
		private const val SCROLL_TO_BOTTOM_DELAY = 300L
		private const val POP_BACKSTACK_DELAY = 1500L
	}
}
