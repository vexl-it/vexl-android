package cz.cleevio.repository.model.chat

import android.os.Parcelable
import com.cleevio.vexl.cryptography.model.KeyPair
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chat(
	val keyPair: KeyPair,
	val offerId: String,
) : Parcelable
