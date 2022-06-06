package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetTriggerDeleteBinding
import cz.cleevio.core.model.DeleteOfferValue
import lightbase.core.extensions.layoutInflater
import java.util.*

//todo: should we have default values for delete trigger?
class DeleteTriggerWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetTriggerDeleteBinding
	private var type: DeleteTimeframe = DeleteTimeframe.NONE

	private var fragmentManager: FragmentManager? = null

	init {
		setupUI()

		binding.deleteTimeframe.setOnClickListener {
			fragmentManager?.let { manager ->
				val bottomSheetDialog = TimeframePickerBottomSheetDialog()
				bottomSheetDialog.setInitialValue(DeleteTimeframe.DAYS)
				bottomSheetDialog.setOnDoneListener { result ->
					type = result
					updateTimeframeText(type)
					bottomSheetDialog.dismiss()
				}
				bottomSheetDialog.show(manager, "NumberPickerBottomSheetDialog")
			}
		}

		updateTimeframeText(type)
	}

	private fun setupUI() {
		binding = WidgetTriggerDeleteBinding.inflate(layoutInflater, this)
	}

	private fun updateTimeframeText(value: DeleteTimeframe) {
		when (value) {
			DeleteTimeframe.NONE -> {
				binding.deleteTimeframe.text = "---"
			}
			DeleteTimeframe.DAYS -> {
				binding.deleteTimeframe.text = context.getString(R.string.widget_trigger_delete_days).lowercase(Locale.getDefault())
			}
			DeleteTimeframe.WEEKS -> {
				binding.deleteTimeframe.text = context.getString(R.string.widget_trigger_delete_weeks).lowercase(Locale.getDefault())
			}
			DeleteTimeframe.MONTHS -> {
				binding.deleteTimeframe.text = context.getString(R.string.widget_trigger_delete_months).lowercase(Locale.getDefault())
			}
		}
	}

	//todo: connect to new offer, when BE ready
	fun getValue(): DeleteOfferValue {
		return DeleteOfferValue(
			value = binding.deleteInput.text.toString().toInt(),
			type = type
		)
	}

	fun setFragmentManager(manager: FragmentManager) {
		fragmentManager = manager
	}
}

enum class DeleteTimeframe {
	NONE, DAYS, WEEKS, MONTHS
}