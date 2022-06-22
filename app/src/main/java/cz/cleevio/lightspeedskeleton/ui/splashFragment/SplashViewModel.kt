package cz.cleevio.lightspeedskeleton.ui.splashFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.network.data.Status
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel

class SplashViewModel constructor(
	private val encryptedPreferences: EncryptedPreferenceRepository,
	private val userRepository: UserRepository,
	val navMainGraphModel: NavMainGraphModel,
	private val offerRepository: OfferRepository,
	private val chatRepository: ChatRepository
) : BaseViewModel() {

	val userFlow = userRepository.getUserFlow()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			val offers = offerRepository.getMyOffersWithoutInbox()
			offers.forEach { myOffer ->
				val inboxResponse = chatRepository.createInbox(myOffer.publicKey)
				if (inboxResponse.status is Status.Success) {
					offerRepository.saveMyOfferIdAndKeys(
						offerId = myOffer.offerId,
						privateKey = myOffer.privateKey,
						publicKey = myOffer.publicKey,
						offerType = myOffer.offerType,
						isInboxCreated = true
					)
				}
			}
		}
	}

	//debug
	fun deletePreviousUserAndLoadKeys() {
		viewModelScope.launch(Dispatchers.IO) {
			resetKeys()
		}
	}

	//debug
	private fun resetKeys() {
		encryptedPreferences.userPrivateKey = ""
		encryptedPreferences.userPublicKey = ""
		encryptedPreferences.hash = ""
		encryptedPreferences.signature = ""
	}
}