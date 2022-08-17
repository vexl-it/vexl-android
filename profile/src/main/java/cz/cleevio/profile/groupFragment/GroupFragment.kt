package cz.cleevio.profile.groupFragment

import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import cz.cleevio.core.utils.RandomUtils
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.core.widget.LeaveGroupBottomSheetDialog
import cz.cleevio.profile.R
import cz.cleevio.profile.databinding.FragmentGroupBinding
import cz.cleevio.repository.model.group.Group
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseFragment
import cz.cleevio.vexl.lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

const val IS_TESTING_GROUPS = false

class GroupFragment : BaseFragment(R.layout.fragment_group) {

	private val binding by viewBinding(FragmentGroupBinding::bind)
	override val viewModel by viewModel<GroupViewModel>()

	lateinit var adapter: GroupAdapter
	private val leaveGroup: (Group) -> Unit = { group ->
		showBottomDialog(LeaveGroupBottomSheetDialog(group.groupUuid))
	}

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.myGroups.collect { groups ->
				adapter.submitList(groups)
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottomWithIME
			)
		}

		viewModel.syncMyGroupsData()
		//adapter
		adapter = GroupAdapter(onLeaveGroup = leaveGroup)
		binding.recycler.adapter = adapter

		binding.close.setOnClickListener {
			findNavController().popBackStack()
		}

		binding.createGroupBtn.isVisible = IS_TESTING_GROUPS
		binding.createGroupBtn.setOnClickListener {
			val name = RandomUtils.generateName()
			Toast.makeText(requireActivity(), "Creating group named $name", Toast.LENGTH_SHORT).show()
			viewModel.createGroup(name, {
				Toast.makeText(requireActivity(), "Group $name created", Toast.LENGTH_SHORT).show()
			})
		}

		binding.joinGroupBtn.setOnClickListener {
			findNavController().navigate(
				GroupFragmentDirections.actionGroupFragmentToCameraFragment()
			)
		}
	}

}