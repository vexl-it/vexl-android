package cz.cleeevio.vexl.contacts.importFacebookContactsFragment

import cz.cleevio.repository.repository.user.UserRepository
import lightbase.core.baseClasses.BaseViewModel


class ImportFacebookContactsViewModel constructor(
	private val userRepository: UserRepository
) : BaseViewModel() {

	val user = userRepository.getUserFlow()

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