package cz.cleevio.core.widget

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cleevio.vexl.cryptography.model.KeyPair
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogBlockUserConfirmBinding
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.repository.repository.chat.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class BlockUserConfirmBottomSheetDialog(
	private val senderPublicKey: String,
	private val publicKeyToBlock: String,
	private val inboxPublicKey: String,
	private val onDismiss: () -> Unit
) : BottomSheetDialogFragment() {

	private val chatRepository: ChatRepository by inject()
	private val coroutineScope = CoroutineScope(Dispatchers.IO)

	private lateinit var binding: BottomSheetDialogBlockUserConfirmBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogBlockUserConfirmBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.confirmBtn.setOnClickListener {
			coroutineScope.launch {
				//delete chat
				chatRepository.sendMessage(
					senderPublicKey = senderPublicKey,
					receiverPublicKey = publicKeyToBlock,
					message = ChatMessage(
						inboxPublicKey = inboxPublicKey,
						senderPublicKey = senderPublicKey,
						recipientPublicKey = publicKeyToBlock,
						text = "TODO: need text?",
						type = MessageType.DELETE_CHAT,
						isMine = true,
						isProcessed = false
					),
					messageType = MessageType.DELETE_CHAT.name
				)

				val result: KeyPair? = chatRepository.getKeyPairByMyPublicKey(senderPublicKey)
				result?.let { senderKeyPair ->
					val response = chatRepository.changeUserBlock(senderKeyPair, publicKeyToBlock, true)
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
						else -> {
							//nothing
						}
					}
				}
			}
		}
		binding.backBtn.setOnClickListener {
			dismiss()
		}
	}

	override fun onDismiss(dialog: DialogInterface) {
		super.onDismiss(dialog)

		onDismiss()
	}
}