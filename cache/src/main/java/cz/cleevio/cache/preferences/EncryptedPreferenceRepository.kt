package cz.cleevio.cache.preferences

interface EncryptedPreferenceRepository {

	var isUserVerified: Boolean
	var userPublicKey: String
	var userPrivateKey: String
	var signature: String
	var hash: String
}