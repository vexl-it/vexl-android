package cz.cleeevio.vexl.contacts.importFacebookContactsFragment

import androidx.fragment.app.Fragment
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import lightbase.core.baseClasses.BaseViewModel
import timber.log.Timber


class ImportFacebookContactsViewModel constructor(
	private val userRepository: UserRepository
) : BaseViewModel() {

	private val facebookCallbackManager: CallbackManager by lazy {
		CallbackManager.Factory.create()
	}

	private val _facebookPermissionApproved = MutableSharedFlow<Boolean>(replay = 1)
	val facebookPermissionApproved = _facebookPermissionApproved.asSharedFlow()

	val user = userRepository.getUserFlow()

	init {
		LoginManager.getInstance().registerCallback(
			facebookCallbackManager,
			object : FacebookCallback<LoginResult> {
				override fun onSuccess(loginResult: LoginResult) {
					Timber.d("loginResult success $loginResult")
					_facebookPermissionApproved.tryEmit(true)
				}

				override fun onCancel() {
					Timber.d("loginResult cancel")
					_facebookPermissionApproved.tryEmit(false)
				}

				override fun onError(exception: FacebookException) {
					Timber.d("loginResult error $exception")
					_facebookPermissionApproved.tryEmit(false)
				}
			})
	}

	fun checkFacebookLogin(fragment: Fragment) {
		if (isLoggedIntoFacebook()) {
			Timber.d("Logged into FB")
			_facebookPermissionApproved.tryEmit(true)
		} else {
			Timber.d("Not logged into FB")
			LoginManager.getInstance().logInWithReadPermissions(
				fragment,
				facebookCallbackManager,
				listOf("user_friends")
			)
		}
	}

	private fun isLoggedIntoFacebook(): Boolean {
		val accessToken = AccessToken.getCurrentAccessToken()
		return accessToken != null && !accessToken.isExpired
	}

}