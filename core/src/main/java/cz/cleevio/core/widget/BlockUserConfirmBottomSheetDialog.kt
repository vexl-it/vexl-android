package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogBlockUserConfirmBinding

class BlockUserConfirmBottomSheetDialog : BottomSheetDialogFragment() {

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
			// TODO block user
			dismiss()
		}
		binding.backBtn.setOnClickListener {
			dismiss()
		}
	}
}