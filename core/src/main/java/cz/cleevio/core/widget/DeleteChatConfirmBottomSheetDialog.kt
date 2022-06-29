package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogDeleteChatConfirmBinding

class DeleteChatConfirmBottomSheetDialog : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogDeleteChatConfirmBinding

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
			// TODO delete chat
			dismiss()
		}
		binding.backBtn.setOnClickListener {
			dismiss()
		}
	}
}