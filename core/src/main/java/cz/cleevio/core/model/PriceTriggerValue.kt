package cz.cleevio.core.model

import cz.cleevio.core.widget.TriggerType
import java.math.BigDecimal

data class PriceTriggerValue constructor(
	val type: TriggerType,
	val value: BigDecimal?,
	val currency: String
)
