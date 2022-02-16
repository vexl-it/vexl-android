package cz.cleevio.pin

import android.widget.Toast
import cz.cleevio.core.utils.viewBinding
import cz.cleevio.pin.databinding.FragmentPinBinding
import lightbase.core.extensions.fadeIn
import lightbase.core.extensions.showToast
import lightbase.pin.LBPinFragment
import lightbase.security.FingerprintBuilder
import lightbase.security.FingerprintErrorInterface
import org.koin.android.ext.android.inject

class PinFragment : LBPinFragment(R.layout.fragment_pin) {

	private val fingerprintBuilder by inject<FingerprintBuilder>()

	private val binding by viewBinding(FragmentPinBinding::bind)

	override val strengthChecker: (String) -> Boolean = { pin ->
		!pin.contains("4")
	}

	private var currentNumber = ""

	override fun bindObservers() = Unit

	override fun initNumpad() {
		setupNumpad(binding.numpad)
	}

	override fun initView() {
		fingerprintBuilder.build(
			requireActivity(),
			successCallback = {
				Toast.makeText(requireContext(), "Fingerprint success", Toast.LENGTH_SHORT).show()
			},
			true,
			errorCallback = object : FingerprintErrorInterface {
				override fun onAuthenticationFailed() {
					Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT)
						.show()
				}

				override fun onFingerprintLockoutEvent() {
					Toast.makeText(requireContext(), "Fingerprint lockout", Toast.LENGTH_SHORT)
						.show()
				}

				override fun onFingerprintReaderMissingEvent() {
					Toast.makeText(
						requireContext(),
						"Fingerprint reader missing",
						Toast.LENGTH_SHORT
					).show()
				}

				override fun onTooManyAttemptsEvent() {
					Toast.makeText(requireContext(), "Too many attempts", Toast.LENGTH_SHORT).show()
				}
			}
		)
	}

	private fun showErrorInLayout(errorDuration: Long) {
		currentNumber = ""
		binding.numpad.setInteractivityState(false)
		binding.numpad.disableRemoveButton()
		binding.pinIndicator.setErrorState()
		binding.pinIndicator.postDelayed({
			binding.numpad.setInteractivityState(true)
			viewModel.reset()
		}, errorDuration)
	}

	override fun onPinWrongListener() {
		binding.pincodeTitle.text = getString(R.string.pin_passcode_missmatch)
		showErrorInLayout(PIN_ERROR_DELAY)
	}

	override fun onPinTooWeakListener() {
		binding.pincodeTitle.text = getString(R.string.pin_too_easy_to_guess)
		showErrorInLayout(PIN_ERROR_DELAY)
	}

	override fun onEnterRepeatState() {
		binding.numpad.setInteractivityState(true)
		binding.pincodeTitle.text = getString(R.string.pin_repeat_password)
		binding.pincodeTitle.fadeIn()
		binding.pinIndicator.resetView()
		binding.numpad.disableRemoveButton()
	}

	override fun onNewPinChosen(pin: String) {
		showToast("Success")
	}

	override fun onResetState() {
		binding.pincodeTitle.text = getString(R.string.pin_create_password)
		viewModel.updateWrittenPin("", strengthChecker)
	}

	override fun onUpdatePinIndicator(text: String) {
		binding.pinIndicator.updateWrittenPin(text)
	}

	override fun shouldFingerPrintIconBeVisible(visible: Boolean) {
		binding.numpad.showFingerprintButton(visible)
	}

	companion object {
		private const val PIN_ERROR_DELAY = 1000L
	}
}
