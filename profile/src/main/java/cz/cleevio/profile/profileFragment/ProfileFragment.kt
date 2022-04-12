package cz.cleevio.profile.profileFragment

import android.widget.Toast
import androidx.core.view.updatePadding
import coil.load
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.profile.R
import cz.cleevio.profile.databinding.FragmentProfileBinding
import lightbase.core.baseClasses.BaseFragment
import lightbase.core.extensions.listenForInsets
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ProfileFragment : BaseFragment(R.layout.fragment_profile) {
	override val viewModel by viewModel<ProfileViewModel>()
	private val binding by viewBinding(FragmentProfileBinding::bind)

	override fun bindObservers() {
		repeatScopeOnStart {
			viewModel.userFlow.collect {
				it?.let { user ->
					binding.profileUserName.text = user.username
					//todo: this should convert from base64 to bitmap
					binding.profileUserPhoto.load(user.avatar) {
						crossfade(true)
						fallback(R.drawable.ic_baseline_person_128)
						error(R.drawable.ic_baseline_person_128)
						placeholder(R.drawable.ic_baseline_person_128)
					}
				}
			}
		}

		repeatScopeOnStart {
			viewModel.contactsNumber.collect {
				binding.profileContacts.setSubtitle(
					getString(R.string.profile_import_contacts_subtitle, it.toString())
				)
			}
		}
	}

	override fun initView() {
		listenForInsets(binding.container) { insets ->
			binding.container.updatePadding(
				top = insets.top,
				bottom = insets.bottom
			)
		}

		binding.profileChangePicture.setOnClickListener {
			Toast.makeText(requireContext(), "Profile picture change not implemented", Toast.LENGTH_SHORT)
				.show()
		}

		binding.profileEditName.setOnClickListener {
			Toast.makeText(requireContext(), "Edit name not implemented", Toast.LENGTH_SHORT)
				.show()
		}

		binding.profileSetPin.setOnClickListener {
			Toast.makeText(requireContext(), "Pin not implemented", Toast.LENGTH_SHORT)
				.show()
		}

		binding.profileContacts.setOnClickListener {
			Toast.makeText(requireContext(), "Contacts import not implemented", Toast.LENGTH_SHORT)
				.show()
		}

		binding.profileFacebook.setOnClickListener {
			Toast.makeText(requireContext(), "Facebook import not implemented", Toast.LENGTH_SHORT)
				.show()
		}

		binding.profileRequestData.setOnClickListener {
			Toast.makeText(requireContext(), "Data request not implemented", Toast.LENGTH_SHORT)
				.show()
		}

		binding.profileLogout.setOnClickListener {
			Timber.tag("ASDX").d("Logout clicked")
			viewModel.logout {
				//todo: move to onboarding?
				Timber.tag("ASDX").d("Logout successful")
			}
		}
	}
}