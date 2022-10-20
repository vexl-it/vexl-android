package cz.cleevio.lightspeedskeleton.ui.maintenance

import androidx.core.view.updatePadding
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.FragmentMaintenanceBinding
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets

class MaintenanceFragment : BaseFragment(R.layout.fragment_maintenance) {

	private val binding by viewBinding(FragmentMaintenanceBinding::bind)

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}
	}

	override fun bindObservers() = Unit
}