package cz.cleevio.core.utils

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun Fragment.getDrawable(id: Int): Drawable? =
	ContextCompat.getDrawable(this.requireContext(), id)

/**
 * @Deprecated use [repeatScopeOnStart] instead
 */
fun Fragment.launchWhenStarted(block: suspend CoroutineScope.() -> Unit): Job =
	viewLifecycleOwner.lifecycleScope.launchWhenStarted(block)

/**
 * @Deprecated use [repeatScopeOnCreate] instead
 */
fun Fragment.launchWhenCreated(block: suspend CoroutineScope.() -> Unit): Job =
	viewLifecycleOwner.lifecycleScope.launchWhenCreated(block)

/**
 * @Deprecated use [repeatScopeOnResume] instead
 */
fun Fragment.launchWhenResumed(block: suspend CoroutineScope.() -> Unit): Job =
	viewLifecycleOwner.lifecycleScope.launchWhenResumed(block)

inline fun Fragment.repeatScopeOnStart(crossinline block: suspend () -> Unit) =
	viewLifecycleOwner.lifecycleScope.launch {
		viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
			block()
		}
	}

inline fun Fragment.repeatScopeOnResume(crossinline block: suspend () -> Unit) =
	viewLifecycleOwner.lifecycleScope.launch {
		viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
			block()
		}
	}

inline fun Fragment.repeatScopeOnCreate(crossinline block: suspend () -> Unit) =
	viewLifecycleOwner.lifecycleScope.launch {
		viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
			block()
		}
	}