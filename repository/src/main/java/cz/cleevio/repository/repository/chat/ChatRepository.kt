package cz.cleevio.repository.repository.chat

import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.user.User

interface ChatRepository {

	suspend fun sendMessage(): Resource<Unit>

	//would flow be better?
	suspend fun loadMessages(userId: Long?): Resource<List<Any>>

	suspend fun loadChatUsers(): Resource<List<User>>

	suspend fun loadChatRequests(): Resource<List<User>>
}