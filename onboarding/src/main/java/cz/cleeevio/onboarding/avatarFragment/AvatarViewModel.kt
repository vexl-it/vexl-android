package cz.cleeevio.onboarding.avatarFragment

import cz.cleevio.repository.repository.user.UserRepository
import lightbase.core.baseClasses.BaseViewModel

class AvatarViewModel constructor(
private val userRepository: UserRepository
) : BaseViewModel()