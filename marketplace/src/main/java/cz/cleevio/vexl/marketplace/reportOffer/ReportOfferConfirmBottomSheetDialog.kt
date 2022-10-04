package cz.cleevio.vexl.marketplace.reportOffer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.vexl.marketplace.databinding.BottomSheetDialogReportOfferConfirmBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportOfferConfirmBottomSheetDialog : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogReportOfferConfirmBinding
	private val viewModel by viewModel<ReportOfferConfirmViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogReportOfferConfirmBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.continueBtn.setOnClickListener {
			dismiss()
			viewModel.navigateToMarketPlace()
		}
	}
}