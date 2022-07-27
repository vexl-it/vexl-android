package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferFriendLevelBinding
import cz.cleevio.core.model.FriendLevelValue
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater

class OfferFriendLevelWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferFriendLevelBinding
	private var selectedButton: FriendLevel = FriendLevel.NONE

	init {
		setupUI()

		binding.friendFirstDegreeWrapper.setOnClickListener {
			redrawWithSelection(FriendLevel.FIRST_DEGREE)
		}

		binding.friendSecondDegreeWrapper.setOnClickListener {
			redrawWithSelection(FriendLevel.SECOND_DEGREE)
		}
	}

	private fun setupUI() {
		binding = WidgetOfferFriendLevelBinding.inflate(layoutInflater, this)
	}

	private fun redrawWithSelection(selection: FriendLevel) {
		selectedButton = selection

		when (selectedButton) {
			FriendLevel.FIRST_DEGREE -> {
				binding.friendFirstDegreeWrapper.setCardBackgroundColor(resources.getColor(R.color.gray_2))
				binding.friendSecondDegreeWrapper.setCardBackgroundColor(resources.getColor(R.color.gray_1))

				binding.friendFirstDegreeCheck.isVisible = true
				binding.friendSecondDegreeCheck.isVisible = false

				binding.friendFirstDegreeText.setTextColor(resources.getColor(R.color.white))
				binding.friendSecondDegreeText.setTextColor(resources.getColor(R.color.gray_3))
			}
			FriendLevel.SECOND_DEGREE -> {
				binding.friendFirstDegreeWrapper.setCardBackgroundColor(resources.getColor(R.color.gray_1))
				binding.friendSecondDegreeWrapper.setCardBackgroundColor(resources.getColor(R.color.gray_2))

				binding.friendFirstDegreeCheck.isVisible = false
				binding.friendSecondDegreeCheck.isVisible = true

				binding.friendFirstDegreeText.setTextColor(resources.getColor(R.color.gray_3))
				binding.friendSecondDegreeText.setTextColor(resources.getColor(R.color.white))
			}
			else -> {
				binding.friendFirstDegreeWrapper.setCardBackgroundColor(resources.getColor(R.color.gray_1))
				binding.friendSecondDegreeWrapper.setCardBackgroundColor(resources.getColor(R.color.gray_1))

				binding.friendFirstDegreeCheck.isVisible = false
				binding.friendSecondDegreeCheck.isVisible = false

				binding.friendFirstDegreeText.setTextColor(resources.getColor(R.color.gray_3))
				binding.friendSecondDegreeText.setTextColor(resources.getColor(R.color.gray_3))
			}
		}
	}

	fun getFriendLevel(): FriendLevelValue = FriendLevelValue(
		value = selectedButton
	)

	fun reset() {
		redrawWithSelection(FriendLevel.NONE)
	}

	fun setValues(button: FriendLevel) {
		selectedButton = button
		redrawWithSelection(selectedButton)
	}
}

enum class FriendLevel {
	NONE, FIRST_DEGREE, SECOND_DEGREE
}