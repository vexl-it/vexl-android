package cz.cleevio.profile.joinGroupFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.group.Group
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class JoinGroupCodeViewModel constructor(
	val groupRepository: GroupRepository
) : BaseViewModel() {

	private val _groupLoaded = MutableSharedFlow<Group>(replay = 1)
	val groupLoaded = _groupLoaded.asSharedFlow()

	fun loadGroupByCode(code: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = groupRepository.getGroupInfoByCode(code)
			when (response.status) {
				is Status.Success -> {
					response.data?.let {
						_groupLoaded.emit(it)
					}
				}
			}
		}
	}
}