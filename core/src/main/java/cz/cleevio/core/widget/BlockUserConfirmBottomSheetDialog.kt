package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cleevio.vexl.cryptography.model.KeyPair
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogBlockUserConfirmBinding
import cz.cleevio.network.data.Status
import cz.cleevio.repository.repository.chat.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class BlockUserConfirmBottomSheetDialog(
	private val senderPublicKey: String,
	private val publicKeyToBlock: String
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
				val result: KeyPair? = chatRepository.getKeyPairByMyPublicKey(senderPublicKey)
				result?.let { senderKeyPair ->
					val response = chatRepository.changeUserBlock(senderKeyPair, publicKeyToBlock, true)
					when (response.status) {
						is Status.Success -> {
							dismiss()
						}
						else -> {
							//network error should be handled automatically
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