package cz.cleevio.repository.repository.group

import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.group.Group
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

//debug
const val WEEK = 7L

interface GroupRepository {

	fun getGroupsFlow(): Flow<List<Group>>

	suspend fun createGroup(
		name: String = "TestGroup",
		logo: String = "https://design.chaincamp.cz/assets/img/logos/chaincamp-symbol-purple-rgb.svg?h=8b40a6ef383113c8e50e13f52566cade",
		expiration: Long = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(WEEK),
		//expiration: Long = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1),
		//closureAt: Long = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(WEEK),
		closureAt: Long = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10),
	): Resource<Group>

	suspend fun syncMyGroups(): Resource<Unit>

	suspend fun joinGroup(code: Long): Resource<Unit>

	suspend fun leaveGroup(groupUuid: String): Resource<Unit>

	suspend fun syncAllGroupsMembers(): Resource<Unit>

	suspend fun syncNewMembersInGroup(groupUuid: String): Resource<Unit>
}