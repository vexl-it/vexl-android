package com.cleevio.vexl.cryptography.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class KeyPair(
	var privateKey: String,
	var publicKey: String
) : Parcelable