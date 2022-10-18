package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogRevealIdentityConfirmBinding

class RevealIdentityBottomSheetDialog(
	private val onApprove: () -> Unit,
	private val onReject: () -> Unit
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogRevealIdentityConfirmBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogRevealIdentityConfirmBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.approveBtn.setOnClickListener {
			onApprove()
			dismiss()
		}
		binding.rejectBtn.setOnClickListener {
			onReject()
			dismiss()
		}
	}

	override fun onStop() {
		dismissAllowingStateLoss()
		super.onStop()
	}
}