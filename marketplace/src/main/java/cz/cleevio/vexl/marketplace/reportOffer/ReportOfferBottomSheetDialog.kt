package cz.cleevio.vexl.marketplace.reportOffer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.setDebouncedOnClickListener
import cz.cleevio.network.data.Status
import cz.cleevio.vexl.lightbase.core.extensions.showSnackbar
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
			viewModel.reportOffer()
			showProgressIndicator(true)
		}
		binding.backBtn.setDebouncedOnClickListener {
			dismiss()
		}

		repeatScopeOnStart {
			viewModel.offerRequest.collect { resource ->
				when (resource.status) {
					is Status.Success -> {
						showProgressIndicator(false)
						val dialog = ReportOfferConfirmBottomSheetDialog()
						dismiss()
						dialog.show(parentFragmentManager, dialog.javaClass.simpleName)
					}
					is Status.Error -> {
						showProgressIndicator(false)
						//show error toast
						resource.errorIdentification.message?.let { messageCode ->
							if (messageCode != -1) {
								showSnackbar(
									view = binding.reportOfferWrapper,
									message = getString(messageCode)
								)
							}
						}
						dismiss()
					}
					else -> Unit
				}
			}
		}
	}

	private fun showProgressIndicator(show: Boolean) {
		binding.progress.isVisible = show
	}
}