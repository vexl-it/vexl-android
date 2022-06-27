package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogMyOfferBinding
import cz.cleevio.repository.model.offer.Offer

class MyOfferBottomSheetDialog(
	private val offer: Offer
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogMyOfferBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogMyOfferBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.offerWidget.bind(item = offer, mode = OfferWidget.Mode.CHAT)
		binding.gotItBtn.setOnClickListener {
			dismiss()
		}
	}
}