package cz.cleevio.vexl.marketplace

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.core.view.isGone
import cz.cleevio.repository.model.offer.LocationSuggestion
import cz.cleevio.vexl.lightbase.core.extensions.layoutInflater
import cz.cleevio.vexl.marketplace.databinding.ItemLocationSuggestionBinding

class LocationSuggestionAdapter constructor(
	private val items: List<LocationSuggestion>,
	activity: Activity
) : ArrayAdapter<LocationSuggestion>(activity, R.layout.item_location_suggestion, items) {

	override fun getView(position: Int, view: View?, parent: ViewGroup): View {
		var convertView = view
		val binding = if (convertView == null) {
			ItemLocationSuggestionBinding.inflate(parent.layoutInflater, parent, false)
		} else {
			ItemLocationSuggestionBinding.bind(convertView)
		}

		binding.city.text = items[position].city
		binding.region.text = items[position].region + ", " + items[position].country
		binding.divider.isGone = position == items.size - 1

		if (convertView == null) convertView = binding.root

		return convertView
	}

	override fun getCount(): Int {
		return items.size
	}

	override fun getItem(position: Int): LocationSuggestion {
		return items[position]
	}

	override fun getFilter(): Filter {
		return object : Filter() {
			override fun performFiltering(constraint: CharSequence?): FilterResults? = null
			override fun publishResults(constraint: CharSequence?, results: FilterResults?) = Unit
		}
	}
}
