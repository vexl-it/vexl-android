package cz.cleeevio.vexl.marketplace.requestOfferFragment

import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.cleeevio.vexl.marketplace.R
import cz.cleeevio.vexl.marketplace.databinding.FragmentRequestOfferBinding
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

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
				it?.let { offer ->
					binding.offerWidget.bind(offer)
					binding.commonFriendsNumber.text = getString(R.string.request_common_friends, offer.commonFriends.size)
					commonFriendsAdapter.submitList(offer.commonFriends.map { friend -> friend.contact })
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

		commonFriendsAdapter = CommonFriendAdapter()
		binding.commonFriendsList.adapter = commonFriendsAdapter

		viewModel.loadOfferById(args.offerId)

		binding.requestOfferBtn.setOnClickListener {
			val offerPublicKey = viewModel.offer.value?.offerPublicKey
			val offerId = viewModel.offer.value?.offerId
			val messageText = binding.requestText.text.toString()

			if (offerPublicKey.isNullOrBlank() || messageText.isBlank() || offerId.isNullOrBlank()) {
				//show error/toast
				if (offerPublicKey.isNullOrBlank()) {
					viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
						Toast.makeText(requireActivity(), "Offer is missing public key", Toast.LENGTH_SHORT).show()
					}
				}

				if (messageText.isBlank()) {
					viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
						Toast.makeText(requireActivity(), "No message detected", Toast.LENGTH_SHORT).show()
					}
				}

				if (offerId.isNullOrBlank()) {
					viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
						Toast.makeText(requireActivity(), "Offer is missing ID", Toast.LENGTH_SHORT).show()
					}
				}
			} else {
				viewModel.sendRequest(
					text = messageText,
					offerPublicKey = offerPublicKey,
					offerId = offerId
				) {
					viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
						Toast.makeText(requireContext(), "Communication request sent successfully", Toast.LENGTH_SHORT)
							.show()
						delay(1500)
						findNavController().popBackStack()
					}
				}
			}
		}

		binding.close.setOnClickListener {
			findNavController().popBackStack()
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
}