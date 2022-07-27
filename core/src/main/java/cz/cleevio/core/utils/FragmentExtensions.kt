package cz.cleevio.core.utils

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cz.cleevio.core.R
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.showToast
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

fun Fragment.sendEmailToSupport(
	email: String? = null,
	subject: String? = null,
	body: String? = null
) {
	val intent = Intent(Intent.ACTION_SENDTO).apply {
		data = Uri.parse("mailto:")
		putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
		putExtra(Intent.EXTRA_SUBJECT, subject)
		putExtra(Intent.EXTRA_TEXT, body)
	}

	try {
		startActivity(intent)
	} catch (e: ActivityNotFoundException) {
		(this as BaseFragment).showToast(
			text = resources.getString(R.string.user_profile_report_issue_error_no_app_found),
			length = Toast.LENGTH_LONG
		)
	}
}