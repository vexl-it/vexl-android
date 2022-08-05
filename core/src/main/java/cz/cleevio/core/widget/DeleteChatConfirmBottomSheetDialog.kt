package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogDeleteChatConfirmBinding
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.repository.repository.chat.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class DeleteChatConfirmBottomSheetDialog constructor(
	private val senderPublicKey: String,
	private val receiverPublicKey: String,
	private val inboxPublicKey: String,
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogDeleteChatConfirmBinding

	private val chatRepository: ChatRepository by inject()
	private val coroutineScope = CoroutineScope(Dispatchers.IO)

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogDeleteChatConfirmBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.confirmBtn.setOnClickListener {
			coroutineScope.launch {
				val response = chatRepository.sendMessage(
					senderPublicKey = senderPublicKey,
					receiverPublicKey = receiverPublicKey,
					message = ChatMessage(
						inboxPublicKey = inboxPublicKey,
						senderPublicKey = senderPublicKey,
						recipientPublicKey = receiverPublicKey,
						text = "TODO: need text?",
						type = MessageType.DELETE_CHAT,
						isMine = true,
						isProcessed = false
					),
					messageType = "DELETE_CHAT"
				)
				when (response.status) {
					is Status.Success -> {
						withContext(Dispatchers.Main) {
							dismiss()
							findNavController().popBackStack()
						}
					}
					is Status.Error -> {
						//todo: handle error?
					}
					else -> Unit
				}
			}
		}
		binding.backBtn.setOnClickListener {
			dismiss()
		}
	}
}