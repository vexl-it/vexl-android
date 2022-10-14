package cz.cleevio.vexl.marketplace.requestOfferFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.chat.ChatMessage
import cz.cleevio.repository.model.chat.MessageType
import cz.cleevio.repository.model.offer.OfferWithGroup
import cz.cleevio.repository.repository.chat.ChatRepository
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class RequestOfferViewModel constructor(
	val groupRepository: GroupRepository,
	private val offerRepository: OfferRepository,
	private val chatRepository: ChatRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository
) : BaseViewModel() {

	private val _isRequesting = MutableStateFlow<Boolean>(false)
	val isRequesting = _isRequesting.asStateFlow()

	private val _offer = MutableStateFlow<OfferWithGroup?>(null)
	val offer = _offer.asStateFlow()

	fun sendRequest(text: String?, offerPublicKey: String, offerId: String, onSuccess: suspend () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			_isRequesting.emit(true)
			val response = chatRepository.askForCommunicationApproval(
				publicKey = offerPublicKey,
				offerId = offerId,
				message = ChatMessage(
					uuid = UUID.randomUUID().toString(),
					inboxPublicKey = encryptedPreferenceRepository.userPublicKey,
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
				else -> Unit
			}

			_isRequesting.emit(false)
		}
	}

	fun loadOfferById(offerId: String) {
		viewModelScope.launch(Dispatchers.IO) {
			offerRepository.getOffersFlow().collect { offers ->
				_offer.emit(
					offers.firstOrNull { it.offerId == offerId }?.let {
						OfferWithGroup(it, groupRepository.findGroupByUuidInDB(it.groupUuids.firstOrNull() ?: ""))
					}
				)
			}
		}
	}
}