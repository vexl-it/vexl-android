package cz.cleevio.core.model

import cz.cleevio.repository.model.offer.PriceTriggerType
import java.math.BigDecimal

data class PriceTriggerValue constructor(
	val type: PriceTriggerType,
	val value: BigDecimal?,
	val currency: String
)
