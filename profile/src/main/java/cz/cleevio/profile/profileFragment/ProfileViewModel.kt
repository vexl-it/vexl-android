package cz.cleevio.profile.profileFragment

import androidx.lifecycle.viewModelScope
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.network.data.Status
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import cz.cleevio.repository.repository.user.UserRepository
import cz.cleevio.vexl.lightbase.core.baseClasses.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository,
	private val offerRepository: OfferRepository,
	val navMainGraphModel: NavMainGraphModel,
) : BaseViewModel() {

	val userFlow = userRepository.getUserFlow()
	private val _contactsNumber = MutableStateFlow<Int>(35)
	val contactsNumber = _contactsNumber.asStateFlow()

	fun logout(onSuccess: () -> Unit = {}, onError: () -> Unit = {}) {
		viewModelScope.launch(Dispatchers.IO) {
			val userDelete = userRepository.deleteMe()
			val contactUserDelete = contactRepository.deleteMyUser()
			val contactFacebookDelete = contactRepository.deleteMyFacebookUser()
			//todo: delete also all offers, when we have system for keeping offer IDs
			val offerDelete = offerRepository.deleteMyOffers(emptyList())

			if (userDelete.status is Status.Success &&
				contactUserDelete.status is Status.Success &&
				contactFacebookDelete.status is Status.Success &&
				offerDelete.status is Status.Success
			) {
				withContext(Dispatchers.Main) {
					onSuccess()
				}
			} else {
				withContext(Dispatchers.Main) {
					onError()
				}
			}
		}
	}

	fun navigateToOnboarding() {
		navMainGraphModel.navigateToGraph(
			NavMainGraphModel.NavGraph.Onboarding
		)
	}
}