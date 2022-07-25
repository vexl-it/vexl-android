package cz.cleevio.profile.donateFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.R
import cz.cleevio.core.databinding.BottomSheetDialogDeleteAccountBinding
import cz.cleevio.core.databinding.BottomSheetDialogDonateBinding

class DonateBottomSheetDialog(
	private val onDonateClicked: ((Boolean) -> Unit)? = null
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogDonateBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogDonateBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.confirmBtn.setOnClickListener {
			onDonateClicked?.invoke(true)
			dismiss()
		}
		binding.backBtn.setOnClickListener {
			onDonateClicked?.invoke(false)
			dismiss()
		}
	}
}