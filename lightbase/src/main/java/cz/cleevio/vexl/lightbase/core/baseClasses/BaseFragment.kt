package cz.cleevio.vexl.lightbase.core.baseClasses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.vexl.lightbase.core.extensions.createProgressDialog
import cz.cleevio.vexl.lightbase.core.extensions.getBottomNavigationViewHeight
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

abstract class BaseFragment constructor(
	contentLayoutId: Int
) : Fragment(contentLayoutId) {

	protected open val viewModel by viewModel<BaseViewModel> { parametersOf() }
	protected open val hasMenu: Boolean = false

	private var progressDialogIndicator: AlertDialog? = null

	abstract fun initView()
	abstract fun bindObservers()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		setHasOptionsMenu(hasMenu)
		return super.onCreateView(inflater, container, savedInstanceState)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initView()
		bindObservers()
	}

	override fun onStop() {
		super.onStop()
		progressDialogIndicator?.dismiss()
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return if (item.itemId == android.R.id.home) {
			handleBackPress()
		} else {
			super.onOptionsItemSelected(item)
		}
	}

	protected open fun handleBackPress(): Boolean = false

	protected fun setupToolbar(
		toolbar: Toolbar,
		title: Int? = null,
		showHomeAsUp: Boolean = false,
		navigationIconId: Int? = null
	) {
		if (title != null) {
			toolbar.title = getString(title)
		} else {
			toolbar.title = ""
		}
		navigationIconId?.let {
			toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), it)
		}
		(activity as AppCompatActivity).setSupportActionBar(toolbar)
		(activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(showHomeAsUp)
	}

	protected open fun setViewForBottomInset(container: View, containsBottomBar: Boolean = false) {
		ViewCompat.setOnApplyWindowInsetsListener(container) { _, insets ->
			container.updatePadding(
				bottom = insets.getInsets(
					WindowInsetsCompat.Type.navigationBars()
				).bottom + if (containsBottomBar) requireActivity().getBottomNavigationViewHeight() else 0
			)
			insets
		}
	}

	protected open fun setViewForTopInset(container: View) {
		ViewCompat.setOnApplyWindowInsetsListener(container) { _, insets ->
			container.updatePadding(
				top = insets.getInsets(
					WindowInsetsCompat.Type.statusBars()
						or WindowInsetsCompat.Type.displayCutout()
				).top
			)
			insets
		}
	}

	protected open fun setViewForTopAndBottomInset(container: View, containsBottomBar: Boolean = false) {
		ViewCompat.setOnApplyWindowInsetsListener(container) { _, insets ->
			container.updatePadding(
				top = insets.getInsets(
					WindowInsetsCompat.Type.statusBars()
						or WindowInsetsCompat.Type.displayCutout()
				).top,
				bottom = insets.getInsets(
					WindowInsetsCompat.Type.navigationBars()
				).bottom + if (containsBottomBar) requireActivity().getBottomNavigationViewHeight() else 0
			)
			insets
		}
	}

	protected open fun setViewForBottomImeInset(view: View, ime: Boolean = true) {
		ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
			v.updatePadding(
				bottom = insets.getInsets(
					if (ime) {
						WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.ime()
					} else {
						WindowInsetsCompat.Type.navigationBars()
					}
				).bottom
			)
			insets
		}
	}

	protected open fun showProgress(visible: Boolean) {
		if (visible) {
			if (progressDialogIndicator == null) {
				progressDialogIndicator = requireContext().createProgressDialog()
			}
			progressDialogIndicator?.show()
		} else {
			progressDialogIndicator?.dismiss()
		}
	}

	protected fun showBottomDialog(dialog: BottomSheetDialogFragment) {
		if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
			dialog.show(childFragmentManager, dialog.javaClass.simpleName)
		}
	}
}
