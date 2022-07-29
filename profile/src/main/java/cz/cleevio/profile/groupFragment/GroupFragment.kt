package cz.cleevio.profile.groupFragment

import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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

		binding.createGroupBtn.setOnClickListener {
			val name = RandomUtils.generateName()
			Toast.makeText(requireActivity(), "Creating group named $name", Toast.LENGTH_SHORT).show()
			viewModel.createGroup(name, {
				Toast.makeText(requireActivity(), "Group $name created", Toast.LENGTH_SHORT).show()
			})
		}

		//debug only: connect button and input field
//		binding.joinGroupBtn.setOnClickListener {
//			val stringCode = binding.joinGroup.text.toString()
//			try {
//				val code = stringCode.toLong()
//				showBottomDialog(
//					JoinGroupBottomSheetDialog(
//						groupName = "TODO: no name",
//						groupLogo = "https://design.chaincamp.cz/assets/img/logos/chaincamp-symbol-purple-rgb.svg?h=8b40a6ef383113c8e50e13f52566cade",
//						groupCode = code,
//					)
//				)
//			} catch (ex: NumberFormatException) {
//				Toast.makeText(requireActivity(), "Code is not valid number", Toast.LENGTH_SHORT).show()
//			}
//		}

		binding.joinGroupBtn.setOnClickListener {
			findNavController().navigate(
				GroupFragmentDirections.actionGroupFragmentToCameraFragment()
			)
		}
	}

	private fun showBottomDialog(dialog: BottomSheetDialogFragment) {
		dialog.show(childFragmentManager, dialog.javaClass.simpleName)
	}

}