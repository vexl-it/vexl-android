package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogDeletePhotoBinding

class DeletePhotoBottomSheetDialog(
	private val onDismissCallback: ((Boolean) -> Unit)? = null
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogDeletePhotoBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogDeletePhotoBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.confirmBtn.setOnClickListener {
			onDismissCallback?.invoke(true)
			dismiss()
		}
		binding.backBtn.setOnClickListener {
			onDismissCallback?.invoke(false)
			dismiss()
		}
	}

	override fun onStop() {
		dismissAllowingStateLoss()
		super.onStop()
	}
}