package cz.cleevio.vexl.chat.chatRequestFragment

import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.chat.CommunicationRequest
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.FragmentChatRequestBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatRequestFragment : BaseFragment(R.layout.fragment_chat_request) {

	private val binding by viewBinding(FragmentChatRequestBinding::bind)
	override val viewModel by viewModel<ChatRequestViewModel>()

	lateinit var adapter: ChatRequestAdapter

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.usersRequestingChat.collect { messages ->
				binding.title.text = getString(R.string.chat_request_main_title, messages.size.toString())
				adapter.submitList(messages)

				binding.declineBtn.isVisible = messages.isNotEmpty()
				binding.acceptBtn.isVisible = messages.isNotEmpty()
			}
		}
		repeatScopeOnStart {
			viewModel.communicationRequestResponse.collect { pair ->
				when (pair.second.status) {
					is Status.Success -> {
						findNavController().navigate(
							ChatRequestFragmentDirections.proceedToChatFragment(pair.first)
						)
					}
				}
			}
		}
	}

	override fun initView() {
		adapter = ChatRequestAdapter()
		binding.requestsRecyclerView.adapter = adapter
		PagerSnapHelper().attachToRecyclerView(binding.requestsRecyclerView)

		binding.close.setOnClickListener {
			findNavController().popBackStack()
		}

		binding.acceptBtn.setOnClickListener {
			val currentRequest = getCurrentChatRequest()
			viewModel.processCommunicationRequest(currentRequest, true)
		}

		binding.declineBtn.setOnClickListener {
			val currentRequest = getCurrentChatRequest()
			viewModel.processCommunicationRequest(currentRequest, false)
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}
	}

	private fun getCurrentChatRequest(): CommunicationRequest {
		val currentIndex = (binding.requestsRecyclerView.layoutManager as LinearLayoutManager)
			.findFirstCompletelyVisibleItemPosition()
		return adapter.getItemAtIndex(currentIndex)
	}
}
