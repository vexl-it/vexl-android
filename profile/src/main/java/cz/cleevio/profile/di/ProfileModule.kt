package cz.cleevio.profile.di

import cz.cleevio.profile.editAvatarFragment.EditAvatarViewModel
import cz.cleevio.profile.editNameFragment.EditNameViewModel
import cz.cleevio.profile.groupFragment.GroupViewModel
import cz.cleevio.profile.profileContactsListFragment.ProfileContactsListViewModel
import cz.cleevio.profile.profileFacebookContactsListFragment.ProfileFacebookContactsListViewModel
import cz.cleevio.profile.joinGroupFragment.JoinGroupCodeViewModel
import cz.cleevio.profile.profileFragment.ProfileViewModel
import cz.cleevio.profile.cameraFragment.CameraViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {

	viewModel {
		ProfileViewModel(
			userRepository = get(),
			contactRepository = get(),
			offerRepository = get(),
			encryptedPreferenceRepository  = get(),
			navMainGraphModel = get()
		)
	}

	viewModel {
		GroupViewModel(
			groupRepository = get(),
		)
	}

	viewModel {
		EditNameViewModel(
			userRepository = get()
		)
	}

	viewModel {
		EditAvatarViewModel(
			userRepository = get(),
			navMainGraphModel = get()
		)
	}

	viewModel {
		JoinGroupCodeViewModel(
			groupRepository = get()
		)
	}

	viewModel {
		CameraViewModel(
			groupRepository = get()
		)
	}

	viewModel {
		ProfileFacebookContactsListViewModel(
			contactRepository = get(),
			navMainGraphModel = get()
		)
	}

	viewModel {
		ProfileContactsListViewModel(
			contactRepository = get(),
			navMainGraphModel = get()
		)
	}
}