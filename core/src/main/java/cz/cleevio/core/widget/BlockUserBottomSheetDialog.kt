package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogBlockUserBinding

class BlockUserBottomSheetDialog(
	private val senderPublicKey: String,
	private val publicKeyToBlock: String
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogBlockUserBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogBlockUserBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.confirmBtn.setOnClickListener {
			val dialog = BlockUserConfirmBottomSheetDialog(
				senderPublicKey = senderPublicKey, publicKeyToBlock = publicKeyToBlock
			)
			dismiss()
			dialog.show(parentFragmentManager, dialog.javaClass.simpleName)
		}
		binding.backBtn.setOnClickListener {
			dismiss()
		}
	}
}