package cz.cleevio.core.utils

import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.widget.FriendLevel
import cz.cleevio.repository.model.contact.ContactKey
import cz.cleevio.repository.model.contact.ContactLevel
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber

class BackgroundQueue constructor(
	private val offerRepository: OfferRepository,
	private val contactRepository: ContactRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val locationHelper: LocationHelper
) : KoinComponent {

	private val _triggerChannel = Channel<Unit>(Channel.CONFLATED)
	val triggerChannel = _triggerChannel.receiveAsFlow()
	private val coroutineScope = CoroutineScope(Dispatchers.IO)
	private var job: Job? = null

	fun triggerBackgroundCheck() {
		//we should try
//		coroutineScope.launch {
//			_triggerChannel.send(Unit)
//		}
		encryptOffersForNewContacts()
	}

	fun encryptOffersForNewContacts() {
		if (job?.isActive == true) {
			Timber.w("Lock prevented multiple encryption cycles")
			return
		}
		Timber.tag("NOTIFICATION").d("Start")

		job = coroutineScope.launch {
			//load keys from DB
			val newMembers: List<ContactKey> = contactRepository.loadNewContacts()
			//load my offers
			val myOffers = offerRepository.getMyOffers()

			myOffers.forEach { myOffer ->
				val commonFriends = contactRepository.getCommonFriends(
					newMembers
						.map { it.key }
						.filter {
							// we don't want to get common friends for our offer
							it != encryptedPreferenceRepository.userPublicKey
						}
				)

				//transform myOffer to Offer with data
				val nullableOffer = offerRepository.getOfferById(myOffer.offerId)
				nullableOffer?.let { offer ->
					val contactsForEncryption = newMembers
						//encrypt offer only for FIRST level contacts or group contact with correct groupUuid
						.filter { contactKey ->
							contactKey.level == ContactLevel.FIRST || contactKey.groupUuid == offer.groupUuid
								|| (contactKey.level == ContactLevel.SECOND && offer.friendLevel == FriendLevel.SECOND_DEGREE.name)
						}
					val encryptedOffers: List<NewOffer> = contactsForEncryption.map { contactKey ->
						OfferUtils.encryptOffer(
							locationHelper = locationHelper,
							offer = offer,
							// orEmpty should not happen, list in map is not nullable
							commonFriends = commonFriends[contactKey.key].orEmpty(),
							contactKey = contactKey.key,
							offerKeys = KeyPair(myOffer.privateKey, myOffer.publicKey),
							groupUuid = contactKey.groupUuid
						)
					}

					//send it to space
					if (encryptedOffers.isNotEmpty()) {
						offerRepository.createOfferForPublicKeys(
							offerId = myOffer.offerId,
							offerList = encryptedOffers,
							additionalEncryptedFor = contactsForEncryption.map { it.key }
						)
					}
				}
			}

			//mark newMembers as processed in DB
			newMembers.forEach { contact ->
				contactRepository.markContactAsProcessed(contact.copy(isUpToDate = true))
			}
			//reset lock
			job = null
			Timber.tag("NOTIFICATION").d("Done")
		}
	}
}