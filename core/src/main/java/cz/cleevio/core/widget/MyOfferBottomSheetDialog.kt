package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogMyOfferBinding
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.repository.repository.group.GroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class MyOfferBottomSheetDialog(
	private val offer: Offer
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogMyOfferBinding
	private val groupRepository: GroupRepository by inject()
	private val coroutineScope = CoroutineScope(Dispatchers.IO)

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
		coroutineScope.launch {
			//TODO: add support for multiple groups
			val group = groupRepository.findGroupByUuidInDB(groupUuid = offer.groupUuids.first())
			withContext(Dispatchers.Main) {
				binding.offerWidget.bind(
					item = offer,
					mode = OfferWidget.Mode.CHAT,
					group = group
				)
			}
		}
		binding.gotItBtn.setOnClickListener {
			dismiss()
		}
	}

	override fun onStop() {
		dismissAllowingStateLoss()
		super.onStop()
	}
}