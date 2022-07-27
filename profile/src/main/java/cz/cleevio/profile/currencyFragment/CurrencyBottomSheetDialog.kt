package cz.cleevio.profile.currencyFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogCurrencyBinding
import cz.cleevio.core.databinding.BottomSheetDialogJoinBinding
import cz.cleevio.core.databinding.BottomSheetDialogReportBinding
import cz.cleevio.core.model.Currency
import cz.cleevio.core.utils.sendEmailToSupport

class CurrencyBottomSheetDialog(
	private val onCurrencyConfirmed: ((Currency) -> Unit)? = null
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogCurrencyBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogCurrencyBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)



		binding.confirmBtn.setOnClickListener {
			onCurrencyConfirmed?.invoke(Currency.CZK)
			dismiss()
		}
	}
}