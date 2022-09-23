package cz.cleevio.lightspeedskeleton.ui.forceUpdate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.view.updatePadding
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.FragmentForceUpdateBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets

class ForceUpdateFragment : BaseFragment(R.layout.fragment_force_update) {

	private val binding by viewBinding(FragmentForceUpdateBinding::bind)

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}

		binding.forceUpdateBtn.setOnClickListener {
			val uri = Uri.parse(MARKET_URL)
			openURI(requireContext(), uri, null)
		}
	}

	override fun bindObservers() = Unit

	private fun openURI(context: Context, uri: Uri?, error: String?) {
		val intent = Intent(Intent.ACTION_VIEW, uri).apply {
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
		}

		if (context.packageManager.queryIntentActivities(intent, 0).size > 0) {
			context.startActivity(intent)
		} else if (error != null) {
			Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
		}
	}

	companion object {
		private const val MARKET_URL = "https://link.vexl.it/join-vexl"
	}
}