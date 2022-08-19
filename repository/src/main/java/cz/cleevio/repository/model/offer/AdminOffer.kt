package cz.cleevio.repository.model.offer

import cz.cleevio.network.response.offer.OfferUnifiedAdminResponse

data class AdminOffer constructor(
	val offerId: String,
	val adminId: String,
	val offerType: String,
)

fun OfferUnifiedAdminResponse.fromNetworkToAdmin(): AdminOffer {
	return AdminOffer(
		offerId = this.offerId,
		adminId = this.adminId,
		offerType = this.offerType.decryptedValue
	)
}