package cz.cleeevio.vexl.marketplace.createOfferFragment

import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import lightbase.core.baseClasses.BaseViewModel


class CreateOfferViewModel constructor(
	private val offerRepository: OfferRepository,
	private val contactRepository: ContactRepository
) : BaseViewModel() {

}