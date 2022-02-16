package cz.cleevio.core.utils

import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker

object DatePickerExtensions {

	fun showDatePicker(
		fragmentManager: FragmentManager,
		selectedDateinMillis: Long?,
		doOnPositive: (Long) -> Unit,
		constraintsBuilder: CalendarConstraints.Builder? = null
	) {
		val datePickerBuilder = MaterialDatePicker.Builder.datePicker()
		datePickerBuilder.setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
		selectedDateinMillis?.let { timeInMillis ->
			datePickerBuilder.setSelection(timeInMillis)
		}

		constraintsBuilder?.let { builder ->
			datePickerBuilder.setCalendarConstraints(builder.build())
		}

		val datePicker = datePickerBuilder.build()
		datePicker.addOnPositiveButtonClickListener { timeInMillis ->
			doOnPositive(timeInMillis)
		}
		datePicker.show(fragmentManager, datePickerBuilder.toString())
	}

	fun showDatePickerEndedWithToday(
		fragmentManager: FragmentManager,
		selectedDateinMillis: Long?,
		doOnPositive: (Long) -> Unit
	) {
		val constraintsBuilder = CalendarConstraints.Builder()
		constraintsBuilder.setEnd(MaterialDatePicker.todayInUtcMilliseconds())
		constraintsBuilder.setValidator(DateValidatorPointBackward.now())

		showDatePicker(
			fragmentManager = fragmentManager,
			selectedDateinMillis = selectedDateinMillis,
			doOnPositive = doOnPositive,
			constraintsBuilder = constraintsBuilder
		)
	}

	fun showDatePickerStartedWithToday(
		fragmentManager: FragmentManager,
		selectedDateinMillis: Long?,
		doOnPositive: (Long) -> Unit
	) {
		val constraintsBuilder = CalendarConstraints.Builder()
		constraintsBuilder.setStart(MaterialDatePicker.todayInUtcMilliseconds())
		constraintsBuilder.setValidator(DateValidatorPointForward.now())

		showDatePicker(
			fragmentManager = fragmentManager,
			selectedDateinMillis = selectedDateinMillis,
			doOnPositive = doOnPositive,
			constraintsBuilder = constraintsBuilder
		)
	}
}