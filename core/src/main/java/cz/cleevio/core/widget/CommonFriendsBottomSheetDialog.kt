package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.databinding.BottomSheetDialogCommonFriendsBinding
import cz.cleevio.repository.model.contact.CommonFriend

class CommonFriendsBottomSheetDialog(
	private val commonFriends: List<CommonFriend>
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogCommonFriendsBinding
	private lateinit var adapter: CommonFriendsDialogAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogCommonFriendsBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		adapter = CommonFriendsDialogAdapter()
		binding.friendsList.adapter = adapter

		adapter.submitList(commonFriends.map { it.contact })

		binding.gotItBtn.setOnClickListener {
			dismiss()
		}
		binding.emptyList.isVisible = commonFriends.isEmpty()
		binding.commonFriendsList.isVisible = commonFriends.isNotEmpty()
	}

	override fun onStop() {
		dismissAllowingStateLoss()
		super.onStop()
	}
}