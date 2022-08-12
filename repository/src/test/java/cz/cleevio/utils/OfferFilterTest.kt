package cz.cleevio.utils

import com.google.common.truth.Truth.assertThat
import cz.cleevio.repository.model.offer.OfferFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test

@ExperimentalCoroutinesApi
class OfferFilterTest {

	@Test
	fun `test price range 1`() {
		val offerFilter = OfferFilter(
			priceRangeBottomLimit = 0f,
			priceRangeTopLimit = 10000f
		)
		val isMatched = offerFilter.isOfferMatchPriceRange(
			minOffer = 0f,
			maxOffer = 25000f
		)
		assertThat(isMatched).isTrue()
	}

	@Test
	fun `test price range 2`() {
		val offerFilter = OfferFilter(
			priceRangeBottomLimit = 16721f,
			priceRangeTopLimit = 72916f
		)
		val isMatched = offerFilter.isOfferMatchPriceRange(
			minOffer = 0f,
			maxOffer = 10000f
		)
		assertThat(isMatched).isFalse()
	}

	@Test
	fun `test price range 3`() {
		val offerFilter = OfferFilter(
			priceRangeBottomLimit = 16721f,
			priceRangeTopLimit = 72916f
		)
		val isMatched = offerFilter.isOfferMatchPriceRange(
			minOffer = 0f,
			maxOffer = 200000f
		)
		assertThat(isMatched).isTrue()
	}

	@Test
	fun `test price range 4`() {
		val offerFilter = OfferFilter(
			priceRangeBottomLimit = 0f,
			priceRangeTopLimit = 10000f
		)
		val isMatched = offerFilter.isOfferMatchPriceRange(
			minOffer = 5000f,
			maxOffer = 25000f
		)
		assertThat(isMatched).isTrue()
	}

	@Test
	fun `test price range 5`() {
		val offerFilter = OfferFilter(
			priceRangeBottomLimit = 25000f,
			priceRangeTopLimit = 34000f
		)
		val isMatched = offerFilter.isOfferMatchPriceRange(
			minOffer = 5000f,
			maxOffer = 12000f
		)
		assertThat(isMatched).isFalse()
	}
}