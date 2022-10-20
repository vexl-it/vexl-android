package cz.cleevio.repository.model.offer

import cz.cleevio.network.response.offer.OfferUnifiedAdminResponse
import cz.cleevio.network.response.offer.v2.OfferUnifiedAdminResponseV2

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

fun OfferUnifiedAdminResponseV2.fromNetworkToAdmin(offerType: String): AdminOffer {
	return AdminOffer(
		offerId = this.offerId,
		adminId = this.adminId,
		offerType = offerType
	)
}