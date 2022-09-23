package cz.cleevio.lightspeedskeleton.ui.splashFragment

import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import cz.cleevio.core.RemoteConfigConstants
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.UserUtils
import cz.cleevio.network.data.Status
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SplashViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	val navMainGraphModel: NavMainGraphModel,
	private val offerRepository: OfferRepository,
	private val chatRepository: ChatRepository,
	private val userUtils: UserUtils,
	private val remoteConfig: FirebaseRemoteConfig
) : BaseViewModel() {

	val userFlow = userRepository.getUserFlow()

	private val _contactKeysLoaded = MutableSharedFlow<Boolean>(replay = 1)
	val contactKeysLoaded = _contactKeysLoaded.asSharedFlow()

	init {
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
						isInboxCreated = true
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

	suspend fun checkForUpdate(): Boolean =
		suspendCoroutine { continuation ->
			remoteConfig.fetch().addOnCompleteListener {
				if (it.isSuccessful) {
					remoteConfig.activate().addOnCompleteListener { activatedTask ->
						if (activatedTask.isSuccessful) {
							continuation.resume(remoteConfig.getBoolean(RemoteConfigConstants.FORCE_UPDATE_SHOWED))
						} else {
							Timber.e(activatedTask.exception)
							continuation.resume(false)
						}
					}
				} else {
					Timber.e(it.exception)
					continuation.resume(false)
				}
			}
		}
}