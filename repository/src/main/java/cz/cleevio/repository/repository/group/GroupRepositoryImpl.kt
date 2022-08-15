package cz.cleevio.repository.repository.group

import cz.cleevio.cache.dao.ContactKeyDao
import cz.cleevio.cache.dao.GroupDao
import cz.cleevio.cache.dao.MyOfferDao
import cz.cleevio.network.api.GroupApi
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.network.extensions.tryOnline
import cz.cleevio.network.request.group.*
import cz.cleevio.network.request.offer.DeletePrivatePartRequest
import cz.cleevio.network.response.group.NewMembersResponse
import cz.cleevio.repository.model.contact.ContactKey
import cz.cleevio.repository.model.contact.toCache
import cz.cleevio.repository.model.group.Group
import cz.cleevio.repository.model.group.fromEntity
import cz.cleevio.repository.model.group.fromNetwork
import cz.cleevio.repository.model.group.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GroupRepositoryImpl constructor(
	val groupApi: GroupApi,
	val groupDao: GroupDao,
	val contactKeyDao: ContactKeyDao,
	val myOfferDao: MyOfferDao,
) : GroupRepository {

	override fun getGroupsFlow(): Flow<List<Group>> = groupDao
		.getAllGroupsFlow().map { list -> list.map { it.fromEntity() } }

	override suspend fun createGroup(
		name: String,
		logo: ImageRequest,
		expiration: Long,
		closureAt: Long
	): Resource<Group> = tryOnline(
		request = {
			groupApi.postGroups(
				CreateGroupRequest(
					name = name,
					logo = logo,
					expirationAt = expiration,
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
				request = {
					groupApi.getGroupsExpired(
						expiredGroupsRequest = ExpiredGroupsRequest(
							uuids = allGroups.map { it.groupUuid })
					)
				},
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

	override suspend fun leaveGroup(groupUuid: String): Resource<DeletePrivatePartRequest> {
		val response = tryOnline(
			request = {
				groupApi.putGroupsLeave(
					LeaveGroupRequest(groupUuid = groupUuid)
				)
			},
			mapper = {}
		)
		return when (response.status) {
			is Status.Success -> {
				//delete also offers created for members of group you just left

				//get all offers IDs
				val offers = myOfferDao.listAll().map { it.extId }

				//get all publicKeys for group
				val groupKeys = contactKeyDao.getKeysByGroup(groupUuid)

				//check that those public keys are not in other contacts
				val validKeys = groupKeys
					.filter {
						//we want to keep only public keys that are not used outside this group
						contactKeyDao.findKeyOutsideThisGroup(it.publicKey, groupUuid) == null
					}
					//and we need only public key to send to BE
					.map { it.publicKey }

				val result = DeletePrivatePartRequest(
					offerId = offers,
					publicKey = validKeys
				)

				groupDao.deleteByUuid(
					groupUuid
				)
				syncMyGroups()

				Resource.success(data = result)
			}
			is Status.Error -> {
				Resource.error(errorIdentification = response.errorIdentification)
			}
			else -> {
				Resource.error(errorIdentification = response.errorIdentification)
			}
		}
	}

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
						contactKeyList.map { it.toCache() }
					)
				}
			}
		)

		return Resource.success(Unit)
	}

	override suspend fun syncNewMembersInGroup(groupUuid: String): Resource<List<ContactKey>> {
		//get keys for group
		val keyContacts = contactKeyDao.getKeysByGroup(groupUuid)
		val response = tryOnline(
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
						contactKeyList.map { it.toCache() }
					)
				}
			}
		)

		return response
	}

	override suspend fun getGroupInfoByCode(code: String): Resource<Group> = tryOnline(
		request = { groupApi.getGroups(code) },
		mapper = { it?.fromNetwork() }
	)

	override suspend fun findGroupByUuidInDB(groupUuid: String): Group? = groupDao.getOneByUuid(groupUuid)?.fromEntity()
}

fun NewMembersResponse.toContactKey(): List<ContactKey> {
	val result: MutableList<ContactKey> = mutableListOf()
	this.newMembers.forEach { groupData ->
		groupData.publicKeys.forEach { publicKey ->
			result.add(
				ContactKey(
					key = publicKey,
					level = cz.cleevio.repository.model.contact.ContactLevel.GROUP,
					groupUuid = groupData.groupUuid
				)
			)
		}
	}
	return result
}