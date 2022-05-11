package cz.cleevio.repository.repository.chat

import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.user.User

interface ChatRepository {

	suspend fun sendMessage(): Resource<Unit>

	suspend fun loadChatUsers(): Resource<List<User>>
}