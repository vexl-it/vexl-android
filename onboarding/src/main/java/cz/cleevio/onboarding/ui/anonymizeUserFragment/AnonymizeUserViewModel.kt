package cz.cleevio.onboarding.ui.anonymizeUserFragment

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.RandomUtils
import cz.cleevio.onboarding.R
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnonymizeUserViewModel constructor(
	val navMainGraphModel: NavMainGraphModel,
	userRepository: UserRepository
) : BaseViewModel() {

	private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.Normal)
	val uiState: StateFlow<UIState> = _uiState.asStateFlow()

	val currentUser = userRepository.getUserFlow()

	fun anonymizeUser(context: Context) {
		viewModelScope.launch(Dispatchers.Default) {
			val newName = RandomUtils.generateName()
			val newAvatar = RandomUtils.selectRandomImage(context)

			//wait until animation is covering the ui
			delay(DELAY)
			_uiState.emit(
				UIState.Anonymized(
					NameWithAvatar(newName, newAvatar)
				)
			)
		}
	}

	data class NameWithAvatar constructor(
		val name: String,
		val avatar: Drawable?
	)

	sealed class UIState {
		object Normal : UIState()
		data class Anonymized constructor(
			val nameWithAvatar: NameWithAvatar
		) : UIState()
	}

	companion object{
		private const val DELAY = 500L
	}
}
