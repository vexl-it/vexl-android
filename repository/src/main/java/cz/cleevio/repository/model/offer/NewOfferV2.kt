package cz.cleevio.repository.model.offer

data class NewOfferV2 constructor(
	val privateParts: List<NewOfferPrivateV2>,
	val payloadPublic: String
)
