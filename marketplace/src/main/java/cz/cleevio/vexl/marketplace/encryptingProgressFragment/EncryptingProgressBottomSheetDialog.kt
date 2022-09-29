package cz.cleevio.vexl.marketplace.encryptingProgressFragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.cleevio.core.model.OfferEncryptionData
import cz.cleevio.core.utils.OfferUtils
import cz.cleevio.core.utils.repeatScopeOnStart
import cz.cleevio.network.data.Resource
import cz.cleevio.repository.model.offer.Offer
import cz.cleevio.vexl.marketplace.R
import cz.cleevio.vexl.marketplace.databinding.BottomSheetDialogEncryptingProgressBinding
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

class EncryptingProgressBottomSheetDialog(
	private val offerEncryptionData: OfferEncryptionData,
	val isNewOffer: Boolean,
	val offerRequest: (Resource<Offer>) -> Unit
) : BottomSheetDialogFragment() {

	private lateinit var binding: BottomSheetDialogEncryptingProgressBinding
	val viewModel by viewModel<EncryptingProgressViewModel> { parametersOf(offerEncryptionData) }

	var numberOfAllContacts: Int = -1

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = BottomSheetDialogEncryptingProgressBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		updateUi(UiState.INIT)
		viewModel.prepareEncryptedOffers()

		repeatScopeOnStart {
			viewModel.newOfferRequest.collect { resource ->
				delay(ENCRYPTION_FINISHED_DELAY)
				updateUi(UiState.DONE)
				delay(DONE_DELAY)
				offerRequest(resource)
			}
		}

		repeatScopeOnStart {
			OfferUtils.offerWasEncryptedForNumberOfContacts.collect { offerWasEncryptedForNumberOfContacts ->
				val contacts = ((offerWasEncryptedForNumberOfContacts * 100.0f) / numberOfAllContacts).roundToInt()
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					binding.progress.setProgress(contacts, true)
				} else {
					binding.progress.progress = contacts
				}

				if (offerWasEncryptedForNumberOfContacts == numberOfAllContacts && offerWasEncryptedForNumberOfContacts != -1) {
					updateUi(UiState.ENCRYPTION_FINISHED)
				} else {
					updateUi(UiState.INIT)
				}
			}
		}

		repeatScopeOnStart {
			OfferUtils.numberOfAllContacts.collect { numberOfAllContacts ->
				this.numberOfAllContacts = numberOfAllContacts
				binding.numberOfVexlers.text = resources.getString(R.string.offer_progress_bar_title, "$numberOfAllContacts")
			}
		}

		repeatScopeOnStart {
			viewModel.encryptedOfferList.collect { encryptedOfferList ->
				when (isNewOffer) {
					true -> viewModel.sendNewOffer(encryptedOfferList)
					false -> offerEncryptionData.offerId?.let { viewModel.sendUpdatedOffer(encryptedOfferList, offerId = it) }
				}
			}
		}
	}

	private fun updateUi(uiState: UiState) {
		when (uiState) {
			UiState.INIT -> {
				binding.title.text = resources.getString(R.string.offer_progress_title_loading)
				binding.numberOfVexlers.text =
					resources.getString(
						R.string.offer_progress_bar_title,
						"${OfferUtils.numberOfAllContacts.value}"
					)
				binding.percentage.text =
					resources.getString(
						R.string.offer_progress_bar_subtitle,
						if (numberOfAllContacts > 0) {
							"${(((OfferUtils.offerWasEncryptedForNumberOfContacts.value * 100.0f) / numberOfAllContacts).roundToInt())}%"
						} else {
							DEFAULT_PERCENTAGE
						}
					)
				binding.subtitle.text = resources.getString(R.string.offer_progress_subtitle)

				binding.percentage.isVisible = true
			}
			UiState.ENCRYPTION_FINISHED -> {
				binding.percentage.text =
					resources.getString(
						R.string.offer_progress_bar_subtitle,
						if (numberOfAllContacts > 0) {
							"${(((OfferUtils.offerWasEncryptedForNumberOfContacts.value * 100.0f) / numberOfAllContacts).roundToInt())}%"
						} else {
							DEFAULT_PERCENTAGE
						}
					)
			}
			UiState.DONE -> {
				binding.title.text = resources.getString(R.string.offer_progress_title_complete)
				binding.numberOfVexlers.text =
					resources.getString(
						R.string.offer_progress_bar_title_complete,
						"${OfferUtils.numberOfAllContacts.value}"
					)
				binding.subtitle.text = resources.getString(R.string.offer_progress_subtitle_complete)

				binding.percentage.isVisible = false
			}
		}
	}

	enum class UiState {
		INIT,
		ENCRYPTION_FINISHED,
		DONE
	}

	companion object {
		private const val DEFAULT_PERCENTAGE = "0%"
		private const val ENCRYPTION_FINISHED_DELAY = 500L
		private const val DONE_DELAY = 3000L
	}
}