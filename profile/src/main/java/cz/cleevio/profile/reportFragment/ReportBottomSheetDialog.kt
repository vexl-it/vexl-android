package cz.cleevio.profile.reportFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogJoinBinding
import cz.cleevio.core.databinding.BottomSheetDialogReportBinding
import cz.cleevio.core.utils.sendEmailToSupport

class ReportBottomSheetDialog : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogReportBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogReportBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.emailWrapper.setOnClickListener {
			sendEmailToSupport(
				email = getString(cz.cleevio.resources.R.string.user_profile_report_issue_email),
				subject = getString(cz.cleevio.resources.R.string.user_profile_report_issue_subject),
				body = getString(cz.cleevio.resources.R.string.user_profile_report_issue_text)
			)
		}

		binding.confirmBtn.setOnClickListener {
			dismiss()
		}
	}

	override fun onStop() {
		dismiss()
		super.onStop()
	}
}