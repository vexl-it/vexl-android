package cz.cleevio.core.utils

import androidx.navigation.*
import timber.log.Timber

fun NavOptionsBuilder.defaultTransition() {
	anim {
		enter = cz.cleevio.core.R.anim.slide_in_right
		exit = cz.cleevio.core.R.anim.slide_out_left
		popEnter = cz.cleevio.core.R.anim.slide_in_left
		popExit = cz.cleevio.core.R.anim.slide_out_right
	}
}

fun NavOptionsBuilder.clearHistory(graphIdForClearing: Int) {
	this.launchSingleTop = true
	popUpTo(graphIdForClearing) {
		inclusive = true
	}
}

fun NavController.safeNavigateWithTransition(directions: NavDirections, clearBackStackUntil: Int? = null) {
	safeNavigate(directions, navOptions {
		defaultTransition()
		if (clearBackStackUntil != null) clearHistory(clearBackStackUntil)
	})
}

fun NavController.safeNavigate(directions: NavDirections, navOptions: NavOptions? = null) {
	val action = currentDestination?.getAction(directions.actionId)
	if (action != null) {
		navigate(directions, navOptions)
	} else {
		Timber.e("Unable to navigate, action is bound to another NavController.")
	}
}