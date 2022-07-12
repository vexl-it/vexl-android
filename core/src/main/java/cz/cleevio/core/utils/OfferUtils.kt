package cz.cleevio.core.utils

import com.cleevio.vexl.cryptography.EciesCryptoLib
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.core.model.OfferParams
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.repository.model.offer.NewOffer

object OfferUtils {

	fun encryptOffer(
		locationHelper: LocationHelper,
		params: OfferParams,
		commonFriends: List<BaseContact>,
		contactKey: String,
		offerKeys: KeyPair
	): NewOffer {
		return NewOffer(
			location = params.location.values.map {
				eciesEncrypt(locationHelper.locationToJsonString(it), contactKey)
			},
			userPublicKey = contactKey,
			offerPublicKey = eciesEncrypt(offerKeys.publicKey, contactKey),
			feeState = eciesEncrypt(params.fee.type.name, contactKey),
			feeAmount = eciesEncrypt(params.fee.value.toString(), contactKey),
			offerDescription = eciesEncrypt(params.description, contactKey),
			amountBottomLimit = eciesEncrypt(params.priceRange.bottomLimit.toString(), contactKey),
			amountTopLimit = eciesEncrypt(params.priceRange.topLimit.toString(), contactKey),
			locationState = eciesEncrypt(params.location.type.name, contactKey),
			paymentMethod = params.paymentMethod.value.map { eciesEncrypt(it.name, contactKey) },
			btcNetwork = params.btcNetwork.value.map { eciesEncrypt(it.name, contactKey) },
			friendLevel = eciesEncrypt(params.friendLevel.value.name, contactKey),
			offerType = eciesEncrypt(params.offerType, contactKey),
			activePriceState = eciesEncrypt(params.priceTrigger.type.name, contactKey),
			activePriceValue = eciesEncrypt(params.priceTrigger.value.toString(), contactKey),
			active = eciesEncrypt(params.active.toString(), contactKey),
			groupUuid = eciesEncrypt(params.groupUuid, contactKey),
			currency = eciesEncrypt(params.currency, contactKey),
			commonFriends = commonFriends.map { friend ->
				eciesEncrypt(friend.getHashedContact(), contactKey)
			}
		)
	}

	private fun eciesEncrypt(data: String, contactKey: String): String =
		EciesCryptoLib.encrypt(contactKey, data)
}