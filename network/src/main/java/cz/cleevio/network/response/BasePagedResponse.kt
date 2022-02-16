package cz.cleevio.network.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BasePagedResponse<T> constructor(
	val currentPage: Int,
	val currentPageSize: Int,
	val pagesTotal: Int,
	val itemsCount: Int,
	val itemsCountTotal: Int,
	val prevLink: String? = null,
	val nextLink: String? = null,
	val items: List<T>? = null
)