package cz.cleevio.profile.groupFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.request.group.ImageRequest
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.group.Group
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

const val WEEK = 7L

class GroupViewModel constructor(
	val groupRepository: GroupRepository
) : BaseViewModel() {

	//fixme: when we have UI for group creation
	private val hardcodedLogo = ImageRequest(
		extension = "png",
		data = "YXNkYXNkc2FkYXNkYXM="
	)
	private val expiration = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(WEEK)
	private val closure = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(WEEK)

	val myGroups = groupRepository.getGroupsFlow()

	fun createGroup(name: String, onSuccess: () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			groupRepository.createGroup(
				name = name,
				logo = hardcodedLogo,
				expiration = expiration,
				closureAt = closure
			)

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
			val response = groupRepository.leaveGroup(groupUuid = group.groupUuid)
			when (response.status) {
				is Status.Success -> {
					//offer
				}
			}
		}
	}

	//debug only - works
	fun joinGroup(code: Long) {
		viewModelScope.launch(Dispatchers.IO) {
			groupRepository.joinGroup(code = code)
		}
	}
}