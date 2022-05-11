package cz.cleevio.vexl.chat.chatFragment

import cz.cleevio.repository.model.user.User
import cz.cleevio.repository.repository.chat.ChatRepository
import lightbase.core.baseClasses.BaseViewModel

class ChatViewModel constructor(
	private val chatRepository: ChatRepository,
	val user: User
) : BaseViewModel() {

	init {
		//load messages for user
	}
}