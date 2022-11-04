package cz.cleevio.repository.model.chat

import android.os.Parcelable
import com.cleevio.vexl.cryptography.model.KeyPair
import cz.cleevio.cache.entity.InboxEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chat constructor(
	val keyPair: KeyPair,
	val offerId: String?,
	val inboxType: InboxType
) : Parcelable

fun InboxEntity.fromCache(): Chat {
	return Chat(
		keyPair = KeyPair(
			publicKey = this.publicKey,
			privateKey = this.privateKey
		),
		offerId = this.offerId,
		inboxType = InboxType.valueOf(this.inboxType)
	)
}