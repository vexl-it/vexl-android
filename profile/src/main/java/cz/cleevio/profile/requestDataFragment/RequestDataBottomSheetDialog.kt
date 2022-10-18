package cz.cleevio.profile.requestDataFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogRequestDataBinding
import cz.cleevio.core.utils.setDebouncedOnClickListener

class RequestDataBottomSheetDialog : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogRequestDataBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogRequestDataBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		/**
		 * TODO adapter with items for downloading
		adapter = CommonFriendsDialogAdapter()
		binding.friendsList.adapter = adapter

		adapter.submitList(commonFriends.map { it.contact })
		 */

		binding.confirmBtn.setDebouncedOnClickListener {
			// todo download data with VM
		}

		binding.confirmBtn.setOnClickListener {
			dismiss()
		}
	}

	override fun onStop() {
		dismissAllowingStateLoss()
		super.onStop()
	}
}