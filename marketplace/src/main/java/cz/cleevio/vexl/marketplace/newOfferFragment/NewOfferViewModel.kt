package cz.cleevio.vexl.marketplace.newOfferFragment

import androidx.lifecycle.viewModelScope
import com.cleevio.vexl.cryptography.KeyPairCryptoLib
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.base.BaseOfferViewModel
import cz.cleevio.core.model.OfferEncryptionData
import cz.cleevio.core.model.OfferParams
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.repository.model.contact.BaseContact
import cz.cleevio.repository.model.contact.ContactKey
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewOfferViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository,
	private val groupRepository: GroupRepository,
	private val encryptedPreferenceRepository: EncryptedPreferenceRepository,
	private val locationHelper: LocationHelper
) : BaseOfferViewModel(
	userRepository,
	contactRepository,
	offerRepository,
	groupRepository,
	encryptedPreferenceRepository
) {

	var isTriggerSectionShowed = true
	var isAdvancedSectionShowed = true

	fun createOffer(params: OfferParams, contactKeys: List<ContactKey>, commonFriends: Map<String, List<BaseContact>>) {
		viewModelScope.launch(Dispatchers.IO) {
			val offerKeys = KeyPairCryptoLib.generateKeyPair()

			super.showEncryptingDialog.emit(
				OfferEncryptionData(
					offerKeys = offerKeys,
					params = params,
					contactRepository = contactRepository,
					encryptedPreferenceRepository = encryptedPreferenceRepository,
					locationHelper = locationHelper,
					contactsPublicKeys = contactKeys,
					commonFriends = commonFriends
				)
			)
		}
	}
}
