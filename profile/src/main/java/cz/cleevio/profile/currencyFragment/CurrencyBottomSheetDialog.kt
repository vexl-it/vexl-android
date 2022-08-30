package cz.cleevio.profile.currencyFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.R
import cz.cleevio.core.databinding.BottomSheetDialogCurrencyBinding
import cz.cleevio.repository.model.Currency

class CurrencyBottomSheetDialog(
	private val currentCurrency: Currency,
	private val onCurrencyConfirmed: ((Currency) -> Unit)? = null
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogCurrencyBinding
	private var currency: Currency? = null

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

		when (currentCurrency) {
			Currency.CZK -> binding.currencyRadioGroup.check(R.id.currency_czk)
			Currency.EUR -> binding.currencyRadioGroup.check(R.id.currency_eur)
			Currency.USD -> binding.currencyRadioGroup.check(R.id.currency_usd)
		}

		binding.currencyRadioGroup.setOnCheckedChangeListener { _, id ->
			currency =
				when (id) {
					R.id.currency_czk -> Currency.CZK
					R.id.currency_eur -> Currency.EUR
					R.id.currency_usd -> Currency.USD
					else -> {
						Currency.CZK
					}
				}
		}

		binding.confirmBtn.setOnClickListener {
			onCurrencyConfirmed?.invoke(currency ?: currentCurrency)
			dismiss()
		}
	}
}