package cz.cleevio.core.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import cz.cleevio.cache.preferences.EncryptedPreferenceRepository
import cz.cleevio.core.R
import cz.cleevio.core.databinding.BottomSheetDialogJoinGroupBinding
import cz.cleevio.network.data.Status
import cz.cleevio.repository.repository.group.GroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

class JoinGroupBottomSheetDialog constructor(
	private val groupCode: Long,
	private val groupName: String,
	private val groupLogo: String,
	private val isFromDeeplink: Boolean = false
) : BottomSheetDialogFragment() {

	private val groupRepository: GroupRepository by inject()
	private val encryptedPreference: EncryptedPreferenceRepository by inject()
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
				if (isFromDeeplink) {
					//we want to clear group code even in case of failure
					encryptedPreference.groupCode = 0L
				}
				when (response.status) {
					is Status.Success -> {
						withContext(Dispatchers.Main) {
							dismiss()
							findNavController().popBackStack()
						}
					}
					is Status.Error -> {
						if (isFromDeeplink) {
							Timber.e("Couldn't join group with deeplink code $groupCode")
							FirebaseCrashlytics.getInstance().recordException(IllegalStateException("Deeplink group join failure"))
						}
					}
					else -> Unit
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
			fallback(R.drawable.random_avatar_4)
			error(R.drawable.random_avatar_4)
			placeholder(R.drawable.random_avatar_4)
		}
	}
}
