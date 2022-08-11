package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.R
import cz.cleevio.core.databinding.BottomSheetDialogDeleteAccountBinding

class DeleteAccountBottomSheetDialog(
	private val onDismissCallback: ((Boolean) -> Unit)? = null
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogDeleteAccountBinding

	private var phase: Int = 0

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogDeleteAccountBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.confirmBtn.setOnClickListener {
			if (phase == 0) {
				binding.title.text = getString(R.string.profile_delete_account_title_sure)
				binding.confirmBtn.text = getString(R.string.profile_delete_account_yes)
				phase = 1
			} else {
				onDismissCallback?.invoke(true)
				dismiss()
			}
		}
		binding.backBtn.setOnClickListener {
			onDismissCallback?.invoke(false)
			dismiss()
		}
	}
}