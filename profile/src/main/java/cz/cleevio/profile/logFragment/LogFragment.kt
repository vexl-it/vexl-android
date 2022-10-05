package cz.cleevio.profile.logFragment

import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.setDebouncedOnClickListener
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.profile.R
import cz.cleevio.profile.databinding.FragmentLogBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class LogFragment : BaseFragment(R.layout.fragment_log) {

	private val binding by viewBinding(FragmentLogBinding::bind)
	override val viewModel by viewModel<LogViewModel>()

	lateinit var adapter: LogAdapter

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(top = insets.top)
		}

		binding.close.setDebouncedOnClickListener {
			findNavController().popBackStack()
		}

		adapter = LogAdapter()
		binding.recycler.adapter = adapter
	}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.logs.collect { logData ->
				//display each log in line?
				Timber.tag("LOGS").d("$logData")
				adapter.submitList(logData)
			}
		}
	}
}