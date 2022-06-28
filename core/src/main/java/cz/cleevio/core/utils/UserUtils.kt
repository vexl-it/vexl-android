package cz.cleevio.core.utils

import cz.cleevio.cache.preferences.EncryptedPreferenceRepository

class UserUtils constructor(
	val encryptedPreferenceRepository: EncryptedPreferenceRepository
) {

	fun resetKeys() {
		encryptedPreferenceRepository.userPrivateKey = ""
		encryptedPreferenceRepository.userPublicKey = ""
		encryptedPreferenceRepository.hash = ""
		encryptedPreferenceRepository.signature = ""
	}
}