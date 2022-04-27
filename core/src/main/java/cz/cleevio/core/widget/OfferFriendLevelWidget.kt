package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferFriendLevelBinding
import cz.cleevio.core.model.FriendLevelValue
import lightbase.core.extensions.layoutInflater
import timber.log.Timber

class OfferFriendLevelWidget constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferFriendLevelBinding
	private var selectedButton: FriendLevel = FriendLevel.NONE

	constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

	init {
		setupUI()

		binding.friendRadiogroup.setOnCheckedChangeListener { _, id ->
			selectedButton = when (id) {
				R.id.friend_first_degree -> {
					FriendLevel.FIRST_DEGREE
				}
				R.id.friend_second_degree -> {
					FriendLevel.SECOND_DEGREE
				}
				else -> {
					Timber.e("Unknown radio ID! '$id'")
					FriendLevel.NONE
				}
			}
		}
	}

	private fun setupUI() {
		binding = WidgetOfferFriendLevelBinding.inflate(layoutInflater, this)
	}

	fun getFriendLevel(): FriendLevelValue = FriendLevelValue(
		value = selectedButton
	)
}

enum class FriendLevel {
	NONE, FIRST_DEGREE, SECOND_DEGREE
}