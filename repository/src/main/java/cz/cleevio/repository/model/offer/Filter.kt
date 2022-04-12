package cz.cleevio.repository.model.offer

import androidx.annotation.DrawableRes

data class Filter(
	val label: String,
	@DrawableRes val icon: Int? = null
)