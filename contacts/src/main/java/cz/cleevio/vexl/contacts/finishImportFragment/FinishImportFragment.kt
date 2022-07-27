package cz.cleevio.vexl.contacts.finishImportFragment

import androidx.lifecycle.lifecycleScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.vexl.contacts.R
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FinishImportFragment : BaseFragment(R.layout.fragment_finish_import) {

	private val binding by viewBinding(cz.cleevio.vexl.contacts.databinding.FragmentFinishImportBinding::bind)
	override val viewModel by viewModel<FinishImportViewModel>()

	override fun bindObservers() {
		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.sshKeyDownloadSuccessful.collect { success ->
				// TODO maybe some delay?
				viewModel.navMainGraphModel.navigateToGraph(
					NavMainGraphModel.NavGraph.Main
				)
			}
		}
	}

	override fun initView() {
		viewModel.loadMyContactsKeys()
	}
}