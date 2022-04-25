package cz.cleeevio.vexl.marketplace.newOfferFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.network.data.Status
import cz.cleevio.repository.model.offer.NewOffer
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel


class NewOfferViewModel constructor(
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository
) : BaseViewModel() {

	fun createOffer(params: NewOfferParams) {
		viewModelScope.launch(Dispatchers.IO) {
			val encryptedOfferList: MutableList<NewOffer> = mutableListOf()

			//load all public keys for specified level of friends
			val contacts = contactRepository.getContactKeys()

			//encrypt in loop for every contact
			contacts.forEach {
				//TODO: encrypt all data fields with each public key
				val encryptedOffer = NewOffer(
					location = params.location.toString(),
					userPublicKey = "",
					offerPublicKey = "",
					direction = "",
					fee = params.fee.toString(),
					amount = params.priceRange.toString(),
					onlyInPerson = "",
					paymentMethod = "",
					typeNetwork = "",
					friendLevel = "",
					offerSymmetricKey = "",
				)
				encryptedOfferList.add(encryptedOffer)
			}

			//send all in single request to BE
			val response = offerRepository.createOffer(encryptedOfferList)
			when (response.status) {
				is Status.Success -> {
					//TODO: notify UI and move user to some other screen?
				}
			}
		}
	}
}