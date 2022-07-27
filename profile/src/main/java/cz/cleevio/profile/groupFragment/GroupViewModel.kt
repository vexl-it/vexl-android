package cz.cleevio.profile.groupFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.repository.model.group.Group
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupViewModel constructor(
	val groupRepository: GroupRepository
) : BaseViewModel() {

	val myGroups = groupRepository.getGroupsFlow()

	fun createGroup(name: String, onSuccess: () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			groupRepository.createGroup(name = name)

			//reload
			syncMyGroupsData()

			withContext(Dispatchers.Main) {
				onSuccess()
			}
		}
	}

	fun syncMyGroupsData() {
		viewModelScope.launch(Dispatchers.IO) {
			groupRepository.syncMyGroups()

			groupRepository.syncAllGroupsMembers()
		}
	}

	//debug only - works
	fun leaveGroup(group: Group) {
		viewModelScope.launch(Dispatchers.IO) {
			groupRepository.leaveGroup(groupUuid = group.groupUuid)
		}
	}

	//debug only - works
	fun joinGroup(code: Long) {
		viewModelScope.launch(Dispatchers.IO) {
			groupRepository.joinGroup(code = code)
		}
	}
}