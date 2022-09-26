package cz.cleevio.core.model

import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.repository.repository.contact.ContactRepository

data class OfferEncryptionData constructor(
	val offerKeys: KeyPair,
	val params: OfferParams,
	val contactRepository: ContactRepository,
	val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	val locationHelper: LocationHelper,
	val offerId: String? = null
)
