package cz.cleevio.vexl.marketplace.reportOffer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.utils.repeatScopeOnResume
import cz.cleevio.core.utils.setDebouncedOnClickListener
import cz.cleevio.vexl.marketplace.databinding.BottomSheetDialogReportOfferBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ReportOfferBottomSheetDialog(
	private val offerId: String
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogReportOfferBinding
	private val viewModel by viewModel<ReportOfferViewModel> {
		parametersOf(offerId)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogReportOfferBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.confirmBtn.setDebouncedOnClickListener {
			requireDialog().setCancelable(false)
			requireDialog().setCanceledOnTouchOutside(false)
			binding.backBtn.setOnClickListener(null)
			binding.confirmBtn.setOnClickListener(null)
			viewModel.reportOffer()
			showProgressIndicator(true)
		}
		binding.backBtn.setDebouncedOnClickListener {
			dismiss()
		}

		repeatScopeOnResume {
			viewModel.offerRequest.collect {
				showProgressIndicator(false)
				val dialog = ReportOfferConfirmBottomSheetDialog()
				dismiss()
				dialog.show(parentFragmentManager, dialog.javaClass.simpleName)
			}
		}
	}

	private fun showProgressIndicator(show: Boolean) {
		binding.progress.isVisible = show
	}
}