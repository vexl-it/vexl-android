package cz.cleevio.vexl.marketplace.marketplaceFragment

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cz.cleevio.core.model.OfferType
import cz.cleevio.vexl.marketplace.marketplaceFragment.offers.OffersBuyFragment
import cz.cleevio.vexl.marketplace.marketplaceFragment.offers.OffersSellFragment
import timber.log.Timber

class MarketplacePagerAdapter constructor(
	fragment: Fragment
) : FragmentStateAdapter(fragment) {

	override fun getItemCount(): Int {
		return 2
	}

	override fun createFragment(position: Int): Fragment {
		return when (position) {
			0 -> {
				OffersBuyFragment()
			}

			1 -> {
				OffersSellFragment()
			}

			else -> {
				Timber.e("createFragment wrong position")
				throw IllegalStateException("MarketplacePagerAdapter received illegal position")
			}
		}
	}

}