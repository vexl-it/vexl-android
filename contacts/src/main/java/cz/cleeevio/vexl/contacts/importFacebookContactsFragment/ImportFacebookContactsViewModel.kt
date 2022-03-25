package cz.cleeevio.vexl.contacts.importFacebookContactsFragment

import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import cz.cleevio.repository.repository.contact.ContactRepository
import cz.cleevio.repository.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lightbase.core.baseClasses.BaseViewModel
import timber.log.Timber


class ImportFacebookContactsViewModel constructor(
	private val userRepository: UserRepository,
	private val contactRepository: ContactRepository
) : BaseViewModel() {

	val facebookCallbackManager: CallbackManager by lazy {
		CallbackManager.Factory.create()
	}

	val user = userRepository.getUserFlow()

	init {
		LoginManager.getInstance().registerCallback(
			facebookCallbackManager,
			object : FacebookCallback<LoginResult> {
				override fun onSuccess(loginResult: LoginResult) {
					Timber.d("loginResult success $loginResult")
					val userId = loginResult.accessToken.userId
					val token = loginResult.accessToken.token
					loginToFacebook(userId, token)
					Timber.d("loginResult success $loginResult")
					// App code
				}

				override fun onCancel() {
					Timber.d("loginResult cancel")
					// App code
				}

				override fun onError(exception: FacebookException) {
					Timber.d("loginResult error $exception")
					// App code
				}
			})
	}

	fun syncFacebookContacts(fragment: Fragment) {
		if (!isLoggedIntoFacebook()) {
			Timber.d("Not logged into FB")
			LoginManager.getInstance().logInWithReadPermissions(
				fragment,
				facebookCallbackManager,
				listOf("user_friends")
			)
		} else {
			AccessToken.getCurrentAccessToken()?.let {
				loginToFacebook(
					it.userId,
					it.token
				)
			}
			Timber.d("Logged into FB")
		}
	}

	fun loginToFacebook(facebookId: String, accessToken: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val response = contactRepository.getFacebookContacts(facebookId, accessToken)
			response.data?.let {
//				_usernameAvailable.emit(it)
			}
		}
	}

	private fun isLoggedIntoFacebook(): Boolean {
		val accessToken = AccessToken.getCurrentAccessToken()
		return accessToken != null && !accessToken.isExpired
	}

	fun loadContacts(accessToken: String) {
//		val request = GraphRequest.newMeRequest(
//			accessToken,
//			object : GraphRequest.GraphJSONObjectCallback() {
//				fun onCompleted(
//					`object`: JSONObject?,
//					response: GraphResponse?
//				) {
//					// Application code
//				}
//			})
//		val parameters = Bundle()
//		parameters.putString("fields", "id,name,link")
//		request.parameters = parameters
//		request.executeAsync()
	}

}