package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogDeleteChatBinding

class DeleteChatBottomSheetDialog(
	private val senderPublicKey: String,
	private val receiverPublicKey: String,
	private val inboxPublicKey: String,
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogDeleteChatBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogDeleteChatBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.confirmBtn.setOnClickListener {
			val dialog = DeleteChatConfirmBottomSheetDialog(
				senderPublicKey = senderPublicKey,
				receiverPublicKey = receiverPublicKey,
				inboxPublicKey = inboxPublicKey
			)
			dismiss()
			dialog.show(parentFragmentManager, dialog.javaClass.simpleName)
		}
		binding.backBtn.setOnClickListener {
			dismiss()
		}
	}
}