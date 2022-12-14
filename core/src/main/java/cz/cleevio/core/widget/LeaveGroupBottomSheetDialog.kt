package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogLeaveGroupBinding
import cz.cleevio.network.data.Status
import cz.cleevio.repository.repository.group.GroupRepository
import cz.cleevio.repository.repository.offer.OfferRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class LeaveGroupBottomSheetDialog constructor(
	private val groupUuid: String
) : BottomSheetDialogFragment() {

	private val groupRepository: GroupRepository by inject()
	private val offerRepository: OfferRepository by inject()
	private val coroutineScope = CoroutineScope(Dispatchers.IO)

	private lateinit var binding: BottomSheetDialogLeaveGroupBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogLeaveGroupBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	@Suppress("UseIfInsteadOfWhen")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.confirmBtn.setOnClickListener {
			coroutineScope.launch(Dispatchers.IO) {
				val response = groupRepository.leaveGroup(groupUuid)
				when (response.status) {
					is Status.Success -> {
						response.data?.let { deleteRequestBody ->
							if (deleteRequestBody.adminIds.isNotEmpty() && deleteRequestBody.publicKeys.isNotEmpty()) {
								offerRepository.deleteOfferForPublicKeys(deleteRequestBody)
							}
						}
						withContext(Dispatchers.Main) {
							dismiss()
						}
					}
					else -> Unit
				}
			}
		}
		binding.backBtn.setOnClickListener {
			dismiss()
		}
		//TODO: we need correct vector icon
	}

	override fun onStop() {
		dismissAllowingStateLoss()
		super.onStop()
	}
}