package cz.cleevio.vexl.chat.chatRequestFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.model.user.User
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.FragmentChatRequestBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.time.ZonedDateTime

class ChatRequestFragment : BaseFragment(R.layout.fragment_chat_request) {

	private val binding by viewBinding(FragmentChatRequestBinding::bind)
	override val viewModel by viewModel<ChatRequestViewModel>()

	private val onAccept: (User) -> Unit = { user ->
		//move to chat
		findNavController().navigate(
			ChatRequestFragmentDirections.proceedToChatFragment(user)
		)
	}
	private val onBlock: (User) -> Unit = {
		//#TODO
		//mark user as blocked?

		//reload users?

		//and show next?

	}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.usersRequestingChat.collect { users ->

				binding.title.text = resources.getString(R.string.chat_request_main_title, users.size)

				//show stacked cards
				//todo: we will also need message and offer
				//fixme: debug data
				var debugDescription = ""
				repeat(4) { debugDescription = "$debugDescription Dlouhy text o offer na nekolik radku, abych videl vetsi card view" }
				val debugOffer = Offer(
					id = 100,
					offerId = "ab123",
					location = listOf(),
					userPublicKey = "",
					offerPublicKey = "",
					offerDescription = debugDescription,
					amountBottomLimit = BigDecimal(100),
					amountTopLimit = BigDecimal(15000),
					feeState = "",
					feeAmount = BigDecimal(0),
					locationState = "",
					paymentMethod = listOf(),
					btcNetwork = listOf(),
					friendLevel = "",
					offerType = "",
					createdAt = ZonedDateTime.now(),
					modifiedAt = ZonedDateTime.now()
				)
				binding.requestView.setData(users, "Tohle je zprava od jineho uzivatele", debugOffer)
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top, bottom = insets.bottom)
		}

		binding.requestView.onAccept = onAccept
		binding.requestView.onBlock = onBlock
	}
}