package cz.cleevio.core.utils

import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.widget.FriendLevel
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber

class BackgroundQueue constructor(
	private val offerRepository: OfferRepository,
	private val contactRepository: ContactRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val offerUtils: OfferUtils
) : KoinComponent {

	private val coroutineScope = CoroutineScope(Dispatchers.IO)
	private var job: Job? = null

	fun triggerBackgroundCheck() {
		reEncryptOffers()
	}

	@Suppress("StringLiteralDuplication", "LongMethod")
	fun reEncryptOffers() {
		if (job?.isActive == true) {
			Timber.tag("REENCRYPT").w("Lock prevented multiple encryption cycles")
			return
		}

		Timber.tag("REENCRYPT").d("reEncryptOffers Start")

		job = coroutineScope.launch {
			//load my offers
			val myOffers = offerRepository.getMyOffers()
			Timber.tag("REENCRYPT").d("loaded ${myOffers.size} offers")

			myOffers.forEach { myOffer ->

				//transform myOffer to Offer with data
				val offer = offerRepository.getOfferById(myOffer.offerId) ?: return@forEach

				Timber.tag("REENCRYPT").d("${myOffer.offerId} is valid offer")

				val allContactsPublicKeys = offerUtils.fetchContactsPublicKeysV2(
					friendLevel = FriendLevel.valueOf(myOffer.friendLevel),
					groupUuids = offer.groupUuids,
					contactRepository = contactRepository,
					encryptedPreferenceRepository = encryptedPreferenceRepository
				)

				Timber.tag("REENCRYPT").d(
					"We have ${allContactsPublicKeys.size} contacts " +
						"for offer level ${FriendLevel.valueOf(myOffer.friendLevel)} and groups ${listOf(offer.groupUuids)}"
				)

				Timber.tag("REENCRYPT").d("My offer is already encrypted for ${myOffer.encryptedFor} contacts")

				//we need to diff between all possible contacts and those that we already encrypted for
				val onlyMissingContacts = allContactsPublicKeys.filter {
					!myOffer.encryptedFor.contains(it.key)
				}

				Timber.tag("REENCRYPT").d("Missing contacts are $onlyMissingContacts")
				//if we have 0 missing contacts, we don't need to continue
				if (onlyMissingContacts.isEmpty()) {
					return@forEach
				}

				val commonFriends = contactRepository.getCommonFriends(
					onlyMissingContacts
						.map { it.key }
						.filter {
							// we don't want to get common friends for our offer
							it != encryptedPreferenceRepository.userPublicKey
						}
				)

				Timber.tag("REENCRYPT").d("commonFriends done")

				//encrypt private parts
				val encryptedPrivatePayloadList = offerUtils.encryptAllPrivatePayloads(
					contactsPublicKeys = onlyMissingContacts,
					commonFriends = commonFriends,
					symmetricalKey = myOffer.symmetricalKey
				)

				Timber.tag("REENCRYPT").d("encryptedOffers count is ${encryptedPrivatePayloadList.size}")

				//send it to space
				if (encryptedPrivatePayloadList.isNotEmpty()) {
					offerRepository.createOfferForPublicKeys(
						offerId = myOffer.offerId,
						offerList = encryptedPrivatePayloadList,
						additionalEncryptedFor = onlyMissingContacts.map { it.key }
					)
				}
			}

			job = null
			Timber.tag("REENCRYPT").d("reEncryptOffers Done")
		}
	}
}