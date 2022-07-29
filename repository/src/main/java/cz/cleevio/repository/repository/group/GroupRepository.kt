package cz.cleevio.repository.repository.group

import cz.cleevio.network.data.Resource
import cz.cleevio.network.request.group.ImageRequest
import cz.cleevio.network.request.offer.DeletePrivatePartRequest
import cz.cleevio.repository.model.contact.ContactKey
import cz.cleevio.repository.model.group.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

	fun getGroupsFlow(): Flow<List<Group>>

	suspend fun createGroup(
		name: String,
		logo: ImageRequest,
		expiration: Long,
		closureAt: Long
	): Resource<Group>

	suspend fun syncMyGroups(): Resource<Unit>

	suspend fun joinGroup(code: Long): Resource<Unit>

	suspend fun leaveGroup(groupUuid: String): Resource<DeletePrivatePartRequest>

	suspend fun syncAllGroupsMembers(): Resource<Unit>

	suspend fun syncNewMembersInGroup(groupUuid: String): Resource<List<ContactKey>>

	suspend fun getGroupInfoByCode(code: String): Resource<Group>

	suspend fun findGroupByUuidInDB(groupUuid: String): Group?
}