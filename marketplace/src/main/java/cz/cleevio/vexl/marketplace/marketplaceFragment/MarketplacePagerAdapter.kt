package cz.cleevio.vexl.marketplace.marketplaceFragment

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cz.cleevio.core.model.OfferType
import cz.cleevio.vexl.marketplace.marketplaceFragment.offers.OffersBuyFragment
import cz.cleevio.vexl.marketplace.marketplaceFragment.offers.OffersSellFragment
import timber.log.Timber

class MarketplacePagerAdapter constructor(
	fragment: Fragment,
	private val navigateToFilters: (OfferType) -> (Unit),
	private val navigateToNewOffer: (OfferType) -> (Unit),
	private val navigateToMyOffers: (OfferType) -> (Unit),
	private val requestOffer: (String) -> (Unit)
) : FragmentStateAdapter(fragment) {

	override fun getItemCount(): Int {
		return 2
	}

	override fun createFragment(position: Int): Fragment {
		return when (position) {
			0 -> {
				OffersBuyFragment(navigateToFilters, navigateToNewOffer, navigateToMyOffers, requestOffer)
			}

			1 -> {
				OffersSellFragment(navigateToFilters, navigateToNewOffer, navigateToMyOffers, requestOffer)
			}

			else -> {
				Timber.e("createFragment wrong position")
				throw IllegalStateException("MarketplacePagerAdapter received illegal position")
			}
		}
	}

}