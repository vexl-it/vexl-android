package cz.cleevio.network.request.contact

data class DeleteContactRequest constructor(
	val contactsToDelete: List<String>
)