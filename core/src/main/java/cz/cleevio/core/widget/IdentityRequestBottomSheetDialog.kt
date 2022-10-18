package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogIdentityRequestBinding

class IdentityRequestBottomSheetDialog constructor(
	private val onSendRequest: () -> Unit
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogIdentityRequestBinding

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
			dismiss()
			onSendRequest()
		}
		binding.backBtn.setOnClickListener {
			dismiss()
		}
	}

	override fun onStop() {
		dismissAllowingStateLoss()
		super.onStop()
	}
}