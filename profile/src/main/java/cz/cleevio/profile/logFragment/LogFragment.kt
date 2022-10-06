package cz.cleevio.profile.logFragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.setDebouncedOnClickListener
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.network.utils.LogData
import cz.cleevio.profile.R
import cz.cleevio.profile.databinding.FragmentLogBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import cz.cleevio.vexl.lightbase.core.extensions.showToast
import org.koin.androidx.viewmodel.ext.android.viewModel

class LogFragment : BaseFragment(R.layout.fragment_log) {

	private val binding by viewBinding(FragmentLogBinding::bind)
	override val viewModel by viewModel<LogViewModel>()

	lateinit var adapter: LogAdapter

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}

		binding.close.setDebouncedOnClickListener {
			findNavController().popBackStack()
		}

		binding.logsSwitch.isChecked = viewModel.encryptedPreferenceRepository.logsEnabled
		binding.logsSwitch.setOnCheckedChangeListener { _, isChecked ->
			viewModel.changeLogs(isChecked)
		}

		binding.logsContinueBtn.setDebouncedOnClickListener {
			copyTextToClipboard(viewModel.logs.value)
		}

		adapter = LogAdapter()
		binding.recycler.adapter = adapter
	}

	private fun copyTextToClipboard(logs: List<LogData>) {
		val text = logs.fold(initial = "") { acc, item -> acc + "${item.timestampDisplay}: " + item.log + "\n" }

		val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		val clip: ClipData = ClipData.newPlainText("Vexl", text)
		clipboard.setPrimaryClip(clip)

		binding.container.showToast(
			requireContext().getString(R.string.logs_text_copied_clipboard)
		)
	}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.logs.collect { logData ->
				//display each log in line?
				adapter.submitList(logData)
			}
		}
	}
}