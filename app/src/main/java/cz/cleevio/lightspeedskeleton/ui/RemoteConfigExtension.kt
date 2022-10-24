package cz.cleevio.lightspeedskeleton.ui

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import cz.cleevio.core.RemoteConfigConstants
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun checkForUpdate(remoteConfig: FirebaseRemoteConfig): Boolean =
	suspendCoroutine { continuation ->
		remoteConfig.fetchAndActivate().addOnCompleteListener {
			continuation.resume(remoteConfig.getBoolean(RemoteConfigConstants.FORCE_UPDATE_SHOWED))
		}
	}

suspend fun checkForMaintenance(remoteConfig: FirebaseRemoteConfig): Boolean =
	suspendCoroutine { continuation ->
		remoteConfig.fetchAndActivate().addOnCompleteListener {
			continuation.resume(remoteConfig.getBoolean(RemoteConfigConstants.MAINTENANCE_SCREEN_SHOWED))
		}
	}