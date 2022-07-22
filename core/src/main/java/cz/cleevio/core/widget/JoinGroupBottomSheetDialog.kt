package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.R
import cz.cleevio.core.databinding.BottomSheetDialogJoinGroupBinding
import cz.cleevio.network.data.Status
import cz.cleevio.repository.repository.group.GroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class JoinGroupBottomSheetDialog constructor(
	private val groupCode: Long,
	private val groupName: String,
	private val groupLogo: String
) : BottomSheetDialogFragment() {

	private val groupRepository: GroupRepository by inject()
	private val coroutineScope = CoroutineScope(Dispatchers.IO)

	private lateinit var binding: BottomSheetDialogJoinGroupBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogJoinGroupBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.confirmBtn.setOnClickListener {
			coroutineScope.launch {
				val response = groupRepository.joinGroup(groupCode)
				when (response.status) {
					is Status.Success -> {
						withContext(Dispatchers.Main) {
							dismiss()
							findNavController().popBackStack()
						}
					}
					is Status.Error -> {
						//todo: handle error?
					}
					else -> {
						//nothing
					}
				}
			}
		}
		binding.backBtn.setOnClickListener {
			dismiss()
		}

		binding.title.text = getString(R.string.groups_join_title, groupName)
		binding.description.text = getString(R.string.groups_join_description, groupName)
		binding.logo.load(groupLogo) {
			crossfade(true)
			//todo: ask for placeholders?
			fallback(R.drawable.ic_baseline_person_128)
			error(R.drawable.ic_baseline_person_128)
			placeholder(R.drawable.ic_baseline_person_128)
		}
	}
}