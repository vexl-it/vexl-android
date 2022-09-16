package cz.cleevio.vexl.lightbase.core.extensions

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.*
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.max

val ViewGroup.layoutInflater: LayoutInflater
	get() = LayoutInflater.from(context)

fun ViewGroup.inflate(@LayoutRes layoutId: Int): View =
	LayoutInflater.from(context)
		.inflate(layoutId, this, false)

fun FragmentActivity.setDarkStatusIcons(applyToStatusBar: Boolean = true, applyToNavigationBar: Boolean = true) {
	if (applyToNavigationBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
		WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true
	}

	if (applyToStatusBar) {
		WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
	}
}

fun FragmentActivity.setLightStatusIcons(applyToStatusBar: Boolean = true, applyToNavigationBar: Boolean = true) {
	if (applyToNavigationBar) {
		WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
	}

	if (applyToStatusBar) {
		WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
	}
}

/**
 * Call this method inside onCreateDialog where you create Dialog instance.
 */
fun BottomSheetDialogFragment.setDarkStatusIcons(
	dialog: Dialog,
	applyToStatusBar: Boolean = true,
	applyToNavigationBar: Boolean = true
) {
	val window = dialog.window ?: return
	if (applyToNavigationBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
		WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true
	}

	if (applyToStatusBar) {
		WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
	}
}

/**
 * Call this method inside onCreateDialog where you create Dialog instance.
 */
fun BottomSheetDialogFragment.setLightStatusIcons(
	dialog: Dialog,
	applyToStatusBar: Boolean = true,
	applyToNavigationBar: Boolean = true
) {
	val window = dialog.window ?: return
	if (applyToNavigationBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
		WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
	}

	if (applyToStatusBar) {
		WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
	}
}

fun FragmentActivity.isSystemInDarkMode(): Boolean {
	return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
		Configuration.UI_MODE_NIGHT_NO -> false
		Configuration.UI_MODE_NIGHT_YES -> true
		else -> false
	}
}

fun Activity.hideKeyboard() {
	if (this.currentFocus != null) {
		val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
	}
}

fun View.hideKeyboard(): Boolean {
	try {
		val inputMethodManager =
			context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
	} catch (ignored: RuntimeException) {
	}
	return false
}

fun View.showKeyboard(): Boolean {
	try {
		val inputMethodManager =
			context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		return inputMethodManager.showSoftInput(this, 0)
	} catch (ignored: RuntimeException) {
	}
	return false
}

fun ImageView.runLoopingAVD(@DrawableRes avdResId: Int) {
	val animated = AnimatedVectorDrawableCompat.create(context, avdResId)
	animated?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
		override fun onAnimationEnd(drawable: Drawable?) {
			this@runLoopingAVD.post { animated.start() }
		}
	})
	this.setImageDrawable(animated)
	animated?.start()
}

fun TextInputEditText.clearErrorOnType(til: TextInputLayout) {
	this.addTextChangedListener {
		til.error = null
	}
}

fun String.openUrl(context: Context, @ColorRes toolbarColor: Int) {
	val builder = CustomTabsIntent.Builder()
	builder.setDefaultColorSchemeParams(
		CustomTabColorSchemeParams.Builder()
			.setToolbarColor(ContextCompat.getColor(context, toolbarColor))
			.build()
	)

	builder.setShowTitle(true)
	builder.setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
	val customTabsIntent = builder.build()
	customTabsIntent.launchUrl(context, Uri.parse(this))
}

fun Fragment.showToast(text: String, length: Int = Toast.LENGTH_SHORT) {
	Toast.makeText(this.requireContext(), text, length).show()
}

fun View.showToast(text: String, length: Int = Toast.LENGTH_SHORT) {
	Toast.makeText(this.context, text, length).show()
}

fun View.fadeIn(duration: Long = 300L, offset: Long = 0L) {
	if (isVisible) return

	this.visibility = View.VISIBLE
	this.startAnimation(AlphaAnimation(0f, 1f).apply {
		this.duration = duration
		this.startOffset = offset
		fillAfter = true
	})

	postDelayed({
		clearAnimation()
	}, duration)
}

fun View.fadeOut(duration: Long = 300L, offset: Long = 0L) {
	val animation = AlphaAnimation(1f, 0f).apply {
		this.duration = duration
		this.startOffset = offset
		fillAfter = true
	}

	postDelayed({
		clearAnimation()
		visibility = View.GONE
	}, duration)

	this.startAnimation(animation)
}

fun View.applyAnimation(@AnimRes id: Int, fillAfter: Boolean = false) {
	val anim = AnimationUtils.loadAnimation(context, id)
	anim.fillAfter = fillAfter
	anim.interpolator = AccelerateDecelerateInterpolator()
	this.startAnimation(anim)
}

fun View.applyAnimationWithDelay(
	@AnimRes id: Int,
	fillAfter: Boolean = false,
	offset: Long = 300L,
	duration: Long = 150L
) {
	val anim = AnimationUtils.loadAnimation(context, id)
	anim.fillAfter = fillAfter
	anim.startOffset = offset
	anim.duration = duration
	anim.interpolator = AccelerateDecelerateInterpolator()
	this.startAnimation(anim)
}

fun Activity.openAppSettings() {
	val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
	val uri = Uri.fromParts("package", this.packageName, null)
	intent.data = uri
	this.startActivity(intent)
}

fun FragmentActivity.listenForIMEInset(view: View, callback: (Int) -> Unit) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
		listenForIMEInsetImpl30(view, callback)
	} else {
		listenForIMEInsetImpl(view, callback)
	}
}

fun Fragment.listenForIMEInset(view: View, callback: (Int) -> Unit) {
	requireActivity().listenForIMEInset(view, callback)
}

@RequiresApi(Build.VERSION_CODES.R)
private fun FragmentActivity.listenForIMEInsetImpl30(view: View, outsideCallback: (Int) -> Unit) {
	val callback = object : WindowInsetsAnimation.Callback(DISPATCH_MODE_STOP) {
		override fun onProgress(insets: WindowInsets, animations: MutableList<WindowInsetsAnimation>): WindowInsets {
			outsideCallback.invoke(
				max(
					insets.getInsets(WindowInsets.Type.ime()).bottom,
					insets.getInsets(WindowInsets.Type.navigationBars()).bottom
				)
			)
			return insets
		}
	}

	listenForInsets(view) {
		outsideCallback.invoke(it.getMaxFromBottom(false))
		ViewCompat.setOnApplyWindowInsetsListener(view, null)
	}
	view.setWindowInsetsAnimationCallback(callback)
}

@RequiresApi(Build.VERSION_CODES.R)
private fun Fragment.listenForIMEInsetImpl30(view: View, callback: (Int) -> Unit) {
	requireActivity().listenForIMEInsetImpl30(view, callback)
}

private fun FragmentActivity.listenForIMEInsetImpl(view: View, callback: (Int) -> Unit) {
	var startingPoint = 0
	ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
		val insetFromBottomWithIME = max(
			insets.getInsets(WindowInsetsCompat.Type.ime()).bottom,
			insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
		)

		if (startingPoint == insetFromBottomWithIME) return@setOnApplyWindowInsetsListener insets

		callback.invoke(insetFromBottomWithIME)
		animateInset(startingPoint, insetFromBottomWithIME, callback)
		startingPoint = insetFromBottomWithIME
		insets
	}
}

private fun Fragment.listenForIMEInsetImpl(view: View, callback: (Int) -> Unit) {
	requireActivity().listenForIMEInsetImpl(view, callback)
}

private fun animateInset(baseMargin: Int, insetFromBottomWithIME: Int, callback: (Int) -> Unit) {
	if (baseMargin == 0) {
		callback.invoke(insetFromBottomWithIME)
		return
	}

	val animator = ValueAnimator.ofInt(baseMargin, insetFromBottomWithIME).also {
		it.duration = 250L
		it.interpolator = DecelerateInterpolator()
		it.addUpdateListener { animator ->
			callback.invoke(animator.animatedValue as Int)
		}
	}
	animator.start()
}

fun FragmentActivity.listenForInsets(view: View, callback: (Insets) -> Unit) {
	ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
		val insetFromTop = insets.getInsets(
			WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
		).top
		val insetFromBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
		val insetFromBottomWithIME = insets.getInsets(
			WindowInsetsCompat.Type.navigationBars() or
				WindowInsetsCompat.Type.ime()
		).bottom

		callback.invoke(
			Insets(
				top = insetFromTop,
				bottom = insetFromBottom,
				bottomWithNavBar = insetFromBottom + getBottomNavigationViewHeight(),
				bottomWithIME = insetFromBottomWithIME
			)
		)
		insets
	}
}

fun Fragment.listenForInsets(view: View, callback: (Insets) -> Unit) {
	requireActivity().listenForInsets(view, callback)
}

fun FragmentActivity.getBottomNavigationViewHeight(): Int {
	val resourceId = resources.getIdentifier("design_bottom_navigation_height", "dimen", packageName)
	return if (resourceId > 0) {
		resources.getDimensionPixelSize(resourceId)
	} else 0
}

data class Insets constructor(
	val top: Int,
	val bottom: Int,
	val bottomWithNavBar: Int,
	val bottomWithIME: Int
) {
	fun getMaxFromBottom(containsNavBar: Boolean = false): Int {
		return if (containsNavBar) {
			maxOf(bottom, bottomWithIME, bottomWithNavBar)
		} else {
			maxOf(bottom, bottomWithIME)
		}
	}
}
