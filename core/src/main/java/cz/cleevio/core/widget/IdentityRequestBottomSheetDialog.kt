package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogIdentityRequestBinding
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.ChatUser
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class IdentityRequestBottomSheetDialog constructor(
	private val senderPublicKey: String,
	private val receiverPublicKey: String,
	private val inboxPublicKey: String,
	private val onSendSuccess: () -> Unit
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogIdentityRequestBinding

	private val chatRepository: ChatRepository by inject()
	private val userRepository: UserRepository by inject()
	private val coroutineScope = CoroutineScope(Dispatchers.IO)

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogIdentityRequestBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.sendBtn.setOnClickListener {
			coroutineScope.launch {
				val user = userRepository.getUser()?.let {
					ChatUser(
						name = it.username,
						image = it.avatar
					)
				}
				val messageType = MessageType.REQUEST_REVEAL
				val response = chatRepository.sendMessage(
					senderPublicKey = senderPublicKey,
					receiverPublicKey = receiverPublicKey,
					message = ChatMessage(
						inboxPublicKey = inboxPublicKey,
						senderPublicKey = senderPublicKey,
						recipientPublicKey = receiverPublicKey,
						type = messageType,
						deanonymizedUser = user,
						isMine = true,
						isProcessed = false
					),
					messageType = messageType.name
				)
				when (response.status) {
					is Status.Success -> {
						withContext(Dispatchers.Main) {
							onSendSuccess()
							dismiss()
						}
					}
				}
			}
		}
		binding.backBtn.setOnClickListener {
			dismiss()
		}
	}
}