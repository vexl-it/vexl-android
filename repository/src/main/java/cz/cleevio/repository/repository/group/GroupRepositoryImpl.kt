package cz.cleevio.repository.repository.group

import com.cleevio.vexl.cryptography.ShaCryptoLib
import cz.cleevio.cache.dao.ContactKeyDao
import cz.cleevio.cache.dao.GroupDao
import cz.cleevio.cache.entity.ContactKeyEntity
import cz.cleevio.cache.entity.ContactLevel
import cz.cleevio.network.api.GroupApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.group.*
import cz.cleevio.network.response.group.NewMembersResponse
import cz.cleevio.repository.model.group.Group
import cz.cleevio.repository.model.group.fromEntity
import cz.cleevio.repository.model.group.fromNetwork
import cz.cleevio.repository.model.group.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class GroupRepositoryImpl constructor(
	val groupApi: GroupApi,
	val groupDao: GroupDao,
	val contactKeyDao: ContactKeyDao
) : GroupRepository {

	override fun getGroupsFlow(): Flow<List<Group>> = groupDao
		.getAllGroupsFlow().map { list -> list.map { it.fromEntity() } }

	override suspend fun createGroup(
		name: String,
		logo: String,
		expiration: Long,
		closureAt: Long
	): Resource<Group> = tryOnline(
		request = {
			groupApi.postGroups(
				CreateGroupRequest(
					name = name,
					logo = logo,
					expiration = expiration,
					closureAt = closureAt,
				)
			)
		},
		mapper = { response -> response?.fromNetwork() },
		doOnSuccess = {
			//save to DB
			it?.let { group ->
				groupDao.replace(group.toEntity())
			}
		}
	)

	override suspend fun syncMyGroups(): Resource<Unit> {
		val myGroupsFromBackend = tryOnline(
			request = { groupApi.getGroupsMe() },
			mapper = { response -> response?.groupResponse?.map { it.fromNetwork() } ?: emptyList() }
		).data ?: emptyList()

		//update all my groups
		groupDao.replaceAll(myGroupsFromBackend.map { it.toEntity() })

		//load all groups from DB
		val allGroups = groupDao.getAllGroups().map { it.fromEntity() }

		//check expired only if we have some groups in DB
		if (allGroups.isNotEmpty()) {
			//find expired groups
			val myExpiredGroups = tryOnline(
				request = { groupApi.getGroupsExpired(allGroups.map { it.groupUuid }) },
				mapper = { response -> response?.groupResponse?.map { it.fromNetwork() } ?: emptyList() }
			)

			//delete expired groups from DB
			myExpiredGroups.data?.forEach {
				groupDao.deleteByUuid(
					it.groupUuid
				)
			}
		}

		//return success
		return Resource.success(Unit)
	}

	override suspend fun joinGroup(code: Long): Resource<Unit> = tryOnline(
		request = {
			groupApi.postGroupsJoin(
				JoinGroupRequest(code = code)
			)
		},
		mapper = { },
		doOnSuccess = {
			//TODO: when BE changes, save new group here
			syncMyGroups()
		}
	)

	override suspend fun leaveGroup(groupUuid: String): Resource<Unit> = tryOnline(
		request = {
			groupApi.putGroupsLeave(
				LeaveGroupRequest(groupUuid = ShaCryptoLib.hash(groupUuid))
			)
		},
		mapper = { },
		doOnSuccess = {
			groupDao.deleteByUuid(
				groupUuid
			)
			syncMyGroups()
		}
	)

	override suspend fun syncAllGroupsMembers(): Resource<Unit> {
		//load all groups from DB
		val allGroups = groupDao.getAllGroups().map { it.fromEntity() }
		tryOnline(
			request = {
				//load all members from BE (publicKeys set as empty means load everything)
				groupApi.postGroupsMembersNew(
					NewMemberRequest(
						groups = allGroups.map {
							GroupRequest(
								groupUuid = it.groupUuid,
								publicKeys = contactKeyDao.getKeysByGroup(it.groupUuid).map { it.publicKey }
							)
						}
					)
				)
			},
			mapper = {
				it?.toContactKey()
			},
			doOnSuccess = {
				it?.let { contactKeyList ->
					contactKeyDao.insertContacts(
						contactKeyList
					)
				}
			}
		)

		return Resource.success(Unit)
	}

	override suspend fun syncNewMembersInGroup(groupUuid: String): Resource<Unit> {
		//get keys for group
		val keyContacts = contactKeyDao.getKeysByGroup(groupUuid)
		tryOnline(
			request = {
				//get new group members for
				groupApi.postGroupsMembersNew(
					NewMemberRequest(
						groups = listOf(
							GroupRequest(
								groupUuid = groupUuid,
								publicKeys = keyContacts.map { it.publicKey }
							)
						)
					)
				)
			},
			mapper = {
				it?.toContactKey()
			},
			doOnSuccess = {
				it?.let { contactKeyList ->
					contactKeyDao.insertContacts(
						contactKeyList
					)
				}
			}
		)

		return Resource.success(Unit)
	}
}

fun NewMembersResponse.toContactKey(): MutableList<ContactKeyEntity> {
	val result: MutableList<ContactKeyEntity> = mutableListOf()
	this.newMembers.forEach { groupData ->
		groupData.publicKeys.forEach { publicKey ->
			result.add(
				ContactKeyEntity(
					publicKey = publicKey,
					contactLevel = ContactLevel.GROUP,
					groupUuid = groupData.groupUuid
				)
			)
		}
	}
	return result
}