package cz.cleevio.vexl.chat.chatFragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.Resources
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.load
import cz.cleevio.core.model.OfferType
import cz.cleevio.core.utils.*
import cz.cleevio.core.widget.*
import cz.cleevio.repository.RandomUtils
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.vexl.chat.R
import cz.cleevio.vexl.chat.databinding.FragmentChatBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForIMEInset
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.lang.Integer.max

class ChatFragment : BaseFragment(R.layout.fragment_chat) {

	private val binding by viewBinding(FragmentChatBinding::bind)
	override val viewModel by viewModel<ChatViewModel> { parametersOf(args.communicationRequest) }

	private val args by navArgs<ChatFragmentArgs>()

	private var isHidingSubmitMessageWrapper: Boolean = false
	private var imeDefaultInsets: Int? = null

	private lateinit var adapter: ChatMessagesAdapter

	//flags
	private var showingDialog = false

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.messages?.collect { messages ->
				adapter.submitList(messages)

				//autoscroll should be enabled only when user is scrolled to the bottom of the chat, seeing latest message
				val layoutManager: LinearLayoutManager = binding.chatRv.layoutManager as LinearLayoutManager
				if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1) {
					binding.chatRv.smoothScrollToPosition(max(messages.size - 1, 0))
				}

				val noneOrOnlyRequestMessage = messages.isEmpty() ||
					(messages.size == 1 && messages.firstOrNull { it.type == MessageType.REQUEST_MESSAGING } != null)
				if (noneOrOnlyRequestMessage && !showingDialog) {
					findNavController().popBackStack()
				}

				binding.submitMessageWrapper.isVisible = !(messages.any { it.type == MessageType.DELETE_CHAT })
			}
		}
		repeatScopeOnStart {
			viewModel.chatUserIdentity?.collect { chatUserIdentity ->
				adapter.updateChatUserIdentity(chatUserIdentity)
				val isDeanonymized = chatUserIdentity?.deAnonymized == true

				if (isDeanonymized) {
					val bitmap = chatUserIdentity?.avatarBase64?.getBitmap()
					val index = chatUserIdentity?.anonymousAvatarImageIndex
					setUserAvatar(
						binding.profileImage,
						bitmap,
						index,
						requireContext()
					)
					setUserAvatar(
						binding.identityRevealRequestedIcon,
						bitmap,
						index,
						requireContext()
					)
					setUserAvatar(
						binding.chatDeleteRequestedIcon,
						bitmap,
						index,
						requireContext()
					)
					setUserAvatar(
						binding.identityRevealSentIcon,
						bitmap,
						index,
						requireContext()
					)

					setupColoredTitle(chatUserIdentity?.name ?: "")
				} else {
					binding.profileImage.load(
						RandomUtils.getRandomImageDrawableId(chatUserIdentity?.anonymousAvatarImageIndex ?: 0),
						imageLoader = ImageLoader.invoke(requireContext())
					) {
						setPlaceholders(cz.cleevio.core.R.drawable.random_avatar_3)
					}
					binding.identityRevealRequestedIcon.load(
						RandomUtils.getRandomImageDrawableId(chatUserIdentity?.anonymousAvatarImageIndex ?: 0),
						imageLoader = ImageLoader.invoke(requireContext())
					) {
						setPlaceholders(cz.cleevio.core.R.drawable.random_avatar_3)
					}
					binding.chatDeleteRequestedIcon.load(
						RandomUtils.getRandomImageDrawableId(chatUserIdentity?.anonymousAvatarImageIndex ?: 0),
						imageLoader = ImageLoader.invoke(requireContext())
					) {
						setPlaceholders(cz.cleevio.core.R.drawable.random_avatar_3)
					}
					binding.identityRevealSentIcon.load(
						RandomUtils.getRandomImageDrawableId(chatUserIdentity?.anonymousAvatarImageIndex ?: 0),
						imageLoader = ImageLoader.invoke(requireContext())
					) {
						setPlaceholders(cz.cleevio.core.R.drawable.random_avatar_3)
					}

					setupColoredTitle(chatUserIdentity?.anonymousUsername ?: "")
				}
				binding.identityRevealedName.text = chatUserIdentity?.name
				binding.chatDeleteRequestedTitle.text = chatUserIdentity?.name ?: chatUserIdentity?.anonymousUsername
				binding.chatDeleteRequestedDescription.text = getString(R.string.chat_delete_subtitle)
				setUserAvatar(
					binding.revealedProfileIcon,
					chatUserIdentity?.avatarBase64?.getBitmap(),
					chatUserIdentity?.anonymousAvatarImageIndex,
					requireContext()
				)
			}
		}
		repeatScopeOnStart {
			viewModel.hasPendingIdentityRevealRequests?.collect { pending ->
				binding.identityRevealRequestedWrapper.isVisible = pending
			}
		}
		repeatScopeOnStart {
			viewModel.hasPendingDeleteChatRequests?.collect { pending ->
				binding.chatDeleteRequestedWrapper.isVisible = pending
			}
		}
		repeatScopeOnStart {
			viewModel.canRequestIdentity?.collect { canRequestIdentity ->
				binding.revealIdentityBtn.isVisible = canRequestIdentity
			}
		}
		repeatScopeOnCreate {
			viewModel.identityRevealed.collect { revealed ->
				if (revealed) {
					startSlideAnimation()
				}
				binding.identityRevealedWrapper.isVisible = revealed
			}
		}
		repeatScopeOnCreate {
			viewModel.requestIdentityFlow.collect { resource ->
				showProgress(resource.isLoading())
				if (resource.isSuccess()) {
					binding.identityRevealSentWrapper.isVisible = true
				}
			}
		}
		repeatScopeOnCreate {
			viewModel.resolveIdentityRevealFlow.collect { resource ->
				showProgress(resource.isLoading())
			}
		}
		repeatScopeOnStart {
			viewModel.animationChannel.collect {
				binding.slideEffect.isVisible = false
			}
		}
		repeatScopeOnStart {
			viewModel.deleteChatFinished.collect {
				findNavController().popBackStack()
			}
		}
	}

	override fun initView() {
		val name = args.communicationRequest.message?.deanonymizedUser?.name ?: run {
			getString(R.string.marketplace_detail_friend_first)
		}

		setupColoredTitle(name)

		binding.messageEdit.setOnEditorActionListener { _, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_SEND) {
				sendMessage()
			}
			false
		}

		adapter = ChatMessagesAdapter(
			deleteChat = { chatMessage ->
				viewModel.deleteChat(chatMessage)
			}
		)
		binding.chatRv.adapter = adapter

		binding.close.setOnClickListener {
			//close this screen
			findNavController().popBackStack()
		}
		binding.sendMessageButton.setOnClickListener {
			sendMessage()
		}
		binding.identityRevealRequestedButton.setOnClickListener {
			showBottomDialog(
				RevealIdentityBottomSheetDialog(
					onApprove = {
						viewModel.resolveIdentityRevealRequest(true)
					},
					onReject = {
						viewModel.resolveIdentityRevealRequest(false)
					}
				)
			)
		}
		binding.myOfferBtn.isVisible = args.communicationRequest.offer != null
		args.communicationRequest.offer?.let { offer ->
			binding.myOfferBtn.setDebouncedOnClickListener {
				hideSubmitMessageWrapper()
				showBottomDialog(MyOfferBottomSheetDialog(offer))
			}
			binding.myOfferBtn.text = if (offer.isMine) {
				getString(R.string.chat_btns_my_offer)
			} else {
				getString(R.string.chat_btns_offer)
			}
		}

		binding.commonFriendsBtn.isVisible = args.communicationRequest.offer != null
		binding.commonFriendsBtn.setDebouncedOnClickListener {
			hideSubmitMessageWrapper()
			showBottomDialog(CommonFriendsBottomSheetDialog(args.communicationRequest.offer?.commonFriends.orEmpty()))
		}
		binding.revealIdentityBtn.setDebouncedOnClickListener {
			hideSubmitMessageWrapper()
			showBottomDialog(
				IdentityRequestBottomSheetDialog(
					onSendRequest = {
						viewModel.requestIdentityReveal()
					}
				))
		}
		binding.revealRequestButton.setOnClickListener {
			binding.identityRevealSentWrapper.isVisible = false
		}
		binding.chatDeleteRequestedButton.setOnClickListener {
			binding.chatDeleteRequestedWrapper.isVisible = false
			viewModel.deleteChat()
		}
		binding.deleteChatBtn.setOnClickListener {
			showingDialog = true
			hideSubmitMessageWrapper()
			viewModel.communicationRequest.message?.inboxPublicKey?.let { inboxPublicKey ->
				showBottomDialog(
					DeleteChatBottomSheetDialog(
						senderPublicKey = viewModel.senderPublicKey,
						receiverPublicKey = viewModel.receiverPublicKey,
						inboxPublicKey = inboxPublicKey
					) { showingDialog = false }
				)
			}
		}
		binding.blockUserBtn.setOnClickListener {
			showingDialog = true
			hideSubmitMessageWrapper()
			viewModel.communicationRequest.message?.inboxPublicKey?.let { inboxPublicKey ->
				showBottomDialog(
					BlockUserBottomSheetDialog(
						senderPublicKey = viewModel.senderPublicKey,
						publicKeyToBlock = viewModel.receiverPublicKey,
						inboxPublicKey = inboxPublicKey
					) { showingDialog = false }
				)
			}
		}
		binding.identityRevealedButton.setOnClickListener {
			binding.identityRevealedWrapper.isVisible = false
		}

		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top)
		}

		listenForIMEInset(binding.submitMessageWrapper) { insets ->
			if (imeDefaultInsets == null) {
				imeDefaultInsets = insets
			}

			if (!isHidingSubmitMessageWrapper) {
				binding.submitMessageWrapper.updateLayoutParams<ViewGroup.MarginLayoutParams> {
					bottomMargin = insets
				}
			}

			binding.chatRv.post {
				binding.chatRv.smoothScrollToPosition(max(adapter.itemCount - 1, 0))
			}
		}
	}

	override fun showProgress(visible: Boolean) {
		binding.progressbar.isVisible = visible
	}

	private fun setupColoredTitle(name: String) {
		val offer = args.communicationRequest.offer
		if (offer != null) {
			val isSell = (offer.offerType == OfferType.SELL.name && !offer.isMine)
				|| (offer.offerType == OfferType.BUY.name && offer.isMine)

			if (isSell) {
				BuySellColorizer.colorizeTransactionType(
					getString(R.string.marketplace_detail_user_sell, name),
					name,
					binding.username,
					R.color.pink_100
				)
			} else {
				BuySellColorizer.colorizeTransactionType(
					getString(R.string.marketplace_detail_user_buy, name),
					name,
					binding.username,
					R.color.green_100
				)
			}
		} else {
			binding.username.text = name
		}
	}

	private fun sendMessage() {
		binding.messageEdit.text.toString().let { message ->
			if (message.isNotBlank()) {
				viewModel.sendMessage(message)
				binding.messageEdit.text?.clear()
			}
		}
	}

	private fun startSlideAnimation() {
		val firstPartAnimation = ObjectAnimator.ofFloat(
			binding.slideEffect,
			TRANSITION_X,
			Resources.getSystem().displayMetrics.widthPixels.toFloat(),
			0f
		).apply {
			duration = resources.getInteger(R.integer.anonymize_duration).toLong()
			interpolator = DecelerateInterpolator()
		}

		val secondPartAnimation = ObjectAnimator.ofFloat(
			binding.slideEffect,
			TRANSITION_X,
			0f,
			-Resources.getSystem().displayMetrics.widthPixels.toFloat()
		).apply {
			duration = resources.getInteger(R.integer.anonymize_duration).toLong()
			interpolator = AccelerateInterpolator()
		}

		val animationSet = AnimatorSet()
		animationSet.playSequentially(firstPartAnimation, secondPartAnimation)

		animationSet.addListener(object : Animator.AnimatorListener {
			override fun onAnimationStart(animation: Animator) {
				binding.slideEffect.isVisible = true
			}

			override fun onAnimationEnd(animation: Animator) {
				//was crashing if user closed screen fast (?)
				lifecycleScope.launch {
					viewModel.animationDone()
				}
			}

			override fun onAnimationCancel(animation: Animator) = Unit
			override fun onAnimationRepeat(animation: Animator) = Unit
		})

		animationSet.start()
	}

	private fun hideSubmitMessageWrapper() {
		isHidingSubmitMessageWrapper = true
		imeDefaultInsets?.let {
			binding.submitMessageWrapper.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				bottomMargin = it
			}
		}
		// Delay because of listenForIMEInset() emits only single value during hiding keyboard (not emits till is hidden)
		lifecycleScope.launch {
			delay(500)
			isHidingSubmitMessageWrapper = false
		}
	}

	private companion object {
		private const val TRANSITION_X = "x"
	}
}
