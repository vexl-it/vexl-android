package cz.cleevio.lightspeedskeleton.ui.splashFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.model.OfferEncryptionData
import cz.cleevio.core.utils.*
import cz.cleevio.core.widget.FriendLevel
import cz.cleevio.network.data.ErrorIdentification
import cz.cleevio.network.data.Resource
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.offer.MyOffer
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import cz.cleevio.vexl.marketplace.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

const val TO_SECONDS = 1000L

class SplashViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	val navMainGraphModel: NavMainGraphModel,
	private val offerRepository: OfferRepository,
	private val chatRepository: ChatRepository,
	private val userUtils: UserUtils,
	val backgroundQueue: BackgroundQueue,
	val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	val offerUtils: OfferUtils,
	val locationHelper: LocationHelper,
	val encryptionUtils: EncryptionUtils,
) : BaseViewModel() {

	val userFlow = userRepository.getUserFlow()

	private val _contactKeysLoaded = MutableSharedFlow<Boolean>(replay = 1)
	val contactKeysLoaded = _contactKeysLoaded.asSharedFlow()

	private val _errorFlow = MutableSharedFlow<Resource<Any>>()
	val errorFlow = _errorFlow.asSharedFlow()

	//Int is index of handled offer
	private val _skipMigrationOnError = Channel<Int>(Channel.CONFLATED)
	val skipMigrationOnError = _skipMigrationOnError.receiveAsFlow()

	//Pair<Index, Data>
	private val showEncryptingDialog = MutableSharedFlow<Pair<Int, OfferEncryptionData>>()
	val showEncryptingDialogFlow = showEncryptingDialog.asSharedFlow()

	var myOffersV1: List<MyOffer> = listOf()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			myOffersV1 = offerRepository.getMyOffers(version = 1)
		}

		viewModelScope.launch(Dispatchers.IO) {
			val offers = offerRepository.getMyOffersWithoutInbox()
			offers.forEach { myOffer ->
				val inboxResponse = chatRepository.createInbox(myOffer.publicKey)
				if (inboxResponse.status is Status.Success) {
					offerRepository.saveMyOfferIdAndKeys(
						offerId = myOffer.offerId,
						adminId = myOffer.adminId,
						privateKey = myOffer.privateKey,
						publicKey = myOffer.publicKey,
						offerType = myOffer.offerType,
						isInboxCreated = true,
						encryptedFor = myOffer.encryptedFor,
						symmetricalKey = myOffer.symmetricalKey,
						friendLevel = myOffer.friendLevel
					)
				}
			}
		}
	}

	fun deletePreviousUserKeys() {
		viewModelScope.launch(Dispatchers.IO) {
			userUtils.resetKeys()
		}
	}

	fun loadMyContactsKeys() {
		viewModelScope.launch(Dispatchers.IO) {
			val success = contactRepository.syncMyContactsKeys()
			_contactKeysLoaded.emit(success)
		}
	}

	fun migrateOfferToV2(myOffer: MyOffer, index: Int) {
		viewModelScope.launch(Dispatchers.IO) {
			val offerKeys = offerRepository.loadOfferKeysByOfferId(offerId = myOffer.offerId)
			val symmetricalKey = encryptionUtils.generateAesSymmetricalKey()
			Timber.tag("ASDX").d("symmetricalKey: $symmetricalKey")

			if (offerKeys == null) {
				_errorFlow.emit(
					Resource.error(
						ErrorIdentification.MessageError(message = R.string.error_missing_offer_keys)
					)
				)
				offerRepository.deleteBrokenMyOffersFromDB(listOf(myOffer.offerId))
				_skipMigrationOnError.send(index)
				return@launch
			}

			val offer = offerRepository.getOfferById(myOffer.offerId)
			if (offer == null) {
				_errorFlow.emit(
					Resource.error(
						ErrorIdentification.MessageError(message = R.string.error_offer_not_found)
					)
				)
				offerRepository.deleteBrokenMyOffersFromDB(listOf(myOffer.offerId))
				_skipMigrationOnError.send(index)
				return@launch
			}

			val contactKeys = offerUtils.fetchContactsPublicKeysV2(
				friendLevel = FriendLevel.valueOf(offer.friendLevel.first()),
				groupUuids = offer.groupUuids,
				contactRepository = contactRepository,
				encryptedPreferenceRepository = encryptedPreferenceRepository,
				shouldEmitContacts = true
			)

			val commonFriends = contactRepository.getCommonFriends(
				contactKeys
					.distinctBy { it.key }
					.map { it.key }
					.filter {
						// we don't want to get common friends for our offer
						it != encryptedPreferenceRepository.userPublicKey
					}
			)

			//this needs to be creation of offer, update will not work
			showEncryptingDialog.emit(
				Pair(
					first = index,
					second = OfferEncryptionData(
						offerKeys = offerKeys,
						offer = offer,
						contactRepository = contactRepository,
						encryptedPreferenceRepository = encryptedPreferenceRepository,
						locationHelper = locationHelper,
						offerId = myOffer.offerId,
						contactsPublicKeys = contactKeys,
						commonFriends = commonFriends,
						symmetricalKey = symmetricalKey,
						friendLevel = offer.friendLevel.first(),
						offerType = myOffer.offerType,
						expiration = System.currentTimeMillis() / TO_SECONDS + getOfferExpiration()
					)
				)
			)
		}
	}

	fun deleteMigratedOfferFromDB(offerId: String) {
		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.deleteBrokenMyOffersFromDB(listOf(offerId))
		}
	}
}