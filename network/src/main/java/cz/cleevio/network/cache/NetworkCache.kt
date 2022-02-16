package cz.cleevio.network.cache

interface NetworkCache {

	var installUUID: String?
	var accessTokenGeneral: String?
	var accessTokenGrant: String?
	var refreshToken: String?
	var firebaseToken: String?
	var firebaseTokenWasSent: Boolean
}