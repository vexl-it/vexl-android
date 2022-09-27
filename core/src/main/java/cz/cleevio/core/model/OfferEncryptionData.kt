package cz.cleevio.core.model

import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.repository.model.contact.ContactKey
import cz.cleevio.repository.repository.contact.ContactRepository

@Suppress("DataClassShouldBeImmutable")
data class OfferEncryptionData constructor(
	val offerKeys: KeyPair,
	val params: OfferParams,
	val contactRepository: ContactRepository,
	val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	val locationHelper: LocationHelper,
	val offerId: String? = null,
	var contactsPublicKeys: List<ContactKey> = listOf()
)
