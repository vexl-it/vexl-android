package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogNumberPickerBinding

class NumberPickerBottomSheetDialog : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogNumberPickerBinding

	private var onDone: ((Int) -> Unit)? = null
	private var initialValue: Int = 0

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogNumberPickerBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setUpViews()
	}

	private fun setUpViews() {
		binding.numberPicker.maxValue = 100
		binding.numberPicker.minValue = 0
		binding.numberPicker.wrapSelectorWheel = false
		binding.numberPicker.value = initialValue

		binding.doneBtn.setOnClickListener {
			onDone?.invoke(getResult())
		}
	}

	fun setInitialValue(value: Int) {
		initialValue = value
	}

	fun setOnDoneListener(listener: (Int) -> Unit) {
		onDone = listener
	}

	private fun getResult(): Int = binding.numberPicker.value
}