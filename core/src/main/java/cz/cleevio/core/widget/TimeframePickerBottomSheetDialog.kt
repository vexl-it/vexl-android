package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.R
import cz.cleevio.core.databinding.BottomSheetDialogTimeframePickerBinding

class TimeframePickerBottomSheetDialog : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogTimeframePickerBinding

	private var onDone: ((DeleteTimeframe) -> Unit)? = null
	private var initialValue: DeleteTimeframe = DeleteTimeframe.DAYS
	private var selectedValue: DeleteTimeframe = initialValue

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogTimeframePickerBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setUpViews()
	}

	private fun setUpViews() {
		binding.timeframeRadiogroup.setOnCheckedChangeListener { _, id ->
			selectedValue = when (id) {
				R.id.timeframe_days -> {
					DeleteTimeframe.DAYS
				}
				R.id.timeframe_weeks -> {
					DeleteTimeframe.WEEKS
				}
				R.id.timeframe_months -> {
					DeleteTimeframe.MONTHS
				}
				else -> {
					DeleteTimeframe.NONE
				}
			}

			updateTextColors(selectedValue)
		}

		binding.doneBtn.setOnClickListener {
			onDone?.invoke(selectedValue)
		}

		drawInitValue()
		updateTextColors(initialValue)
	}

	private fun updateTextColors(value: DeleteTimeframe) {
		when (value) {
			DeleteTimeframe.DAYS -> {
				binding.timeframeDays.setTextColor(requireContext().getColor(R.color.black))
				binding.timeframeWeeks.setTextColor(requireContext().getColor(R.color.gray_3))
				binding.timeframeMonths.setTextColor(requireContext().getColor(R.color.gray_3))
			}
			DeleteTimeframe.WEEKS -> {
				binding.timeframeDays.setTextColor(requireContext().getColor(R.color.gray_3))
				binding.timeframeWeeks.setTextColor(requireContext().getColor(R.color.black))
				binding.timeframeMonths.setTextColor(requireContext().getColor(R.color.gray_3))
			}
			DeleteTimeframe.MONTHS -> {
				binding.timeframeDays.setTextColor(requireContext().getColor(R.color.gray_3))
				binding.timeframeWeeks.setTextColor(requireContext().getColor(R.color.gray_3))
				binding.timeframeMonths.setTextColor(requireContext().getColor(R.color.black))
			}
			else -> {
				//do nothing
			}
		}
	}

	private fun drawInitValue() {
		when (initialValue) {
			DeleteTimeframe.DAYS -> {
				binding.timeframeDays.isChecked = true
			}
			DeleteTimeframe.WEEKS -> {
				binding.timeframeWeeks.isChecked = true
			}
			DeleteTimeframe.MONTHS -> {
				binding.timeframeMonths.isChecked = true
			}
			else -> {
				//do nothing
			}
		}
	}

	fun setInitialValue(value: DeleteTimeframe) {
		initialValue = value
	}

	fun setOnDoneListener(listener: (DeleteTimeframe) -> Unit) {
		onDone = listener
	}
}