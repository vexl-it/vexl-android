package cz.cleevio.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil.ImageLoader
import coil.load
import coil.loadAny
import cz.cleevio.core.R
import cz.cleevio.core.databinding.WidgetOfferFriendLevelBinding
import cz.cleevio.core.model.FriendLevelValue
import cz.cleevio.core.utils.setPlaceholders
import cz.cleevio.repository.RandomUtils
import cz.cleevio.repository.model.contact.ContactLevel
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater

class OfferFriendLevelWidget @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: WidgetOfferFriendLevelBinding
	private var selectedFriendLevels: MutableSet<FriendLevel> = mutableSetOf()
	private var isMultichoiceEnabled: Boolean = false

	init {
		setupUI()
		handleFriendLevelSelection(FriendLevel.SECOND_DEGREE)

		binding.friendFirstDegreeWrapper.setOnClickListener {
			handleFriendLevelSelection(FriendLevel.FIRST_DEGREE)
		}

		binding.friendSecondDegreeWrapper.setOnClickListener {
			handleFriendLevelSelection(FriendLevel.SECOND_DEGREE)
		}
	}

	fun isMultichoiceEnabled(isEnabled: Boolean) {
		isMultichoiceEnabled = isEnabled
	}

	fun setUserAvatar(avatar: Any?, anonymousAvatarImageIndex: Int?) {
		if (avatar == null) {
			if (anonymousAvatarImageIndex != null) {
				binding.firstDegreeAvatar.load(
					drawableResId = RandomUtils.getRandomImageDrawableId(anonymousAvatarImageIndex),
					imageLoader = ImageLoader.invoke(context)
				) {
					setPlaceholders(R.drawable.random_avatar_3)
				}
				binding.secondDegreeAvatar.load(
					drawableResId = RandomUtils.getRandomImageDrawableId(anonymousAvatarImageIndex),
					imageLoader = ImageLoader.invoke(context)
				) {
					setPlaceholders(R.drawable.random_avatar_3)
				}
			} else {
				binding.firstDegreeAvatar.load(R.drawable.random_avatar_3, imageLoader = ImageLoader.invoke(context))
				binding.secondDegreeAvatar.load(R.drawable.random_avatar_3, imageLoader = ImageLoader.invoke(context))
			}
		} else {
			binding.firstDegreeAvatar.loadAny(avatar) {
				setPlaceholders(R.drawable.random_avatar_3)
			}
			binding.secondDegreeAvatar.loadAny(avatar) {
				setPlaceholders(R.drawable.random_avatar_3)
			}
		}
	}

	private fun setupUI() {
		binding = WidgetOfferFriendLevelBinding.inflate(layoutInflater, this)
	}

	private fun handleFriendLevelSelection(level: FriendLevel) {
		if (!isMultichoiceEnabled) {
			selectedFriendLevels = mutableSetOf(level)
			redrawWithSelection(level)
		} else {
			if (selectedFriendLevels.contains(level)) {
				selectedFriendLevels.remove(level)
				redrawWithUnselection(level)
			} else {
				selectedFriendLevels.add(level)
				redrawWithSelection(level)
			}
		}
	}

	private fun redrawWithSelection(selection: FriendLevel) {
		when (selection) {
			FriendLevel.FIRST_DEGREE -> {
				binding.friendFirstDegreeWrapper.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_2))
				binding.friendFirstDegreeCheck.isVisible = true
				binding.friendFirstDegreeText.setTextColor(ContextCompat.getColor(context, R.color.white))

				if (!isMultichoiceEnabled) {
					binding.friendSecondDegreeWrapper.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_1))
					binding.friendSecondDegreeCheck.isVisible = false
					binding.friendSecondDegreeText.setTextColor(ContextCompat.getColor(context, R.color.gray_3))
				}
			}
			FriendLevel.SECOND_DEGREE -> {
				binding.friendSecondDegreeWrapper.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_2))
				binding.friendSecondDegreeCheck.isVisible = true
				binding.friendSecondDegreeText.setTextColor(ContextCompat.getColor(context, R.color.white))

				if (!isMultichoiceEnabled) {
					binding.friendFirstDegreeWrapper.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_1))
					binding.friendFirstDegreeCheck.isVisible = false
					binding.friendFirstDegreeText.setTextColor(ContextCompat.getColor(context, R.color.gray_3))
				}
			}
			else -> {
				binding.friendFirstDegreeWrapper.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_1))
				binding.friendFirstDegreeCheck.isVisible = false
				binding.friendFirstDegreeText.setTextColor(ContextCompat.getColor(context, R.color.gray_3))

				binding.friendSecondDegreeWrapper.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_1))
				binding.friendSecondDegreeCheck.isVisible = false
				binding.friendSecondDegreeText.setTextColor(ContextCompat.getColor(context, R.color.gray_3))
			}
		}
	}

	private fun redrawWithUnselection(unselection: FriendLevel) {
		when (unselection) {
			FriendLevel.FIRST_DEGREE -> {
				binding.friendFirstDegreeWrapper.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_1))
				binding.friendFirstDegreeCheck.isVisible = false
				binding.friendFirstDegreeText.setTextColor(ContextCompat.getColor(context, R.color.gray_3))
			}
			FriendLevel.SECOND_DEGREE -> {
				binding.friendSecondDegreeWrapper.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_1))
				binding.friendSecondDegreeCheck.isVisible = false
				binding.friendSecondDegreeText.setTextColor(ContextCompat.getColor(context, R.color.gray_3))
			}
			else -> {
				// Do nothing
			}
		}
	}

	fun getSingleChoiceFriendLevelValue(): FriendLevelValue = FriendLevelValue(
		value = selectedFriendLevels.firstOrNull() ?: FriendLevel.NONE
	)

	fun getMultichoiceFriendLevels(): Set<FriendLevel> = selectedFriendLevels

	fun reset() {
		setValues(setOf(FriendLevel.SECOND_DEGREE))
	}

	fun resetFromFilters() {
		setValues(setOf())
		redrawWithSelection(FriendLevel.NONE)
	}

	fun setValues(levels: Set<FriendLevel>) {
		selectedFriendLevels = levels.toMutableSet()
		levels.forEach { level ->
			redrawWithSelection(level)
		}
	}
}

enum class FriendLevel {
	NONE, FIRST_DEGREE, SECOND_DEGREE, GROUP
}

fun ContactLevel.toFriendLevel(): FriendLevel {
	return when (this) {
		ContactLevel.FIRST -> FriendLevel.FIRST_DEGREE
		ContactLevel.SECOND -> FriendLevel.SECOND_DEGREE
		ContactLevel.GROUP -> FriendLevel.GROUP
		ContactLevel.NOT_SPECIFIED -> FriendLevel.NONE
	}
}

fun FriendLevel.toContactLevel(): ContactLevel {
	return when (this) {
		FriendLevel.FIRST_DEGREE -> ContactLevel.FIRST
		FriendLevel.SECOND_DEGREE -> ContactLevel.SECOND
		FriendLevel.GROUP -> ContactLevel.GROUP
		FriendLevel.NONE -> ContactLevel.NOT_SPECIFIED
	}
}