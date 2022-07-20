package cz.cleeevio.vexl.marketplace.requestOfferFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lightbase.core.baseClasses.BaseViewModel
import java.util.*


class RequestOfferViewModel constructor(
	private val offerRepository: OfferRepository,
	private val chatRepository: ChatRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository
) : BaseViewModel() {

	private val _isRequesting = MutableStateFlow<Boolean>(false)
	val isRequesting = _isRequesting.asStateFlow()

	private val _offer = MutableStateFlow<Offer?>(null)
	val offer = _offer.asStateFlow()

	fun sendRequest(text: String, offerPublicKey: String, offerId: String, onSuccess: suspend () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			_isRequesting.emit(true)
			val response = chatRepository.askForCommunicationApproval(
				publicKey = offerPublicKey,
				offerId = offerId,
				message = ChatMessage(
					uuid = UUID.randomUUID().toString(),
					inboxPublicKey = offerPublicKey,
					senderPublicKey = encryptedPreferenceRepository.userPublicKey,
					recipientPublicKey = offerPublicKey,
					text = text,
					type = MessageType.REQUEST_MESSAGING,
					time = System.currentTimeMillis(),
					isMine = true,
					isProcessed = false
				)
			)
			when (response.status) {
				is Status.Success -> {
					withContext(Dispatchers.Main) {
						onSuccess()
					}
				}
				is Status.Error -> {
					//any special handling?
				}
				else -> {
					//nothing
				}
			}

			_isRequesting.emit(false)
		}
	}

	fun loadOfferById(offerId: String) {
		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.getOffersFlow().collect { offers ->
				_offer.emit(
					offers.firstOrNull { it.offerId == offerId }
				)
			}
		}
	}
}