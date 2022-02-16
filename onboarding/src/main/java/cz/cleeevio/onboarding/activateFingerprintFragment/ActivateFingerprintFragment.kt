package cz.cleeevio.onboarding.activateFingerprintFragment

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import cz.cleeevio.onboarding.R
import cz.cleeevio.onboarding.databinding.FragmentActivateFingerprintBinding
import cz.cleevio.core.utils.viewBinding
import lightbase.core.extensions.isFingerprintAvailable
import lightbase.core.extensions.showToast
import lightbase.onboarding.AbstractActivateFingerprintFragment

class ActivateFingerprintFragment : AbstractActivateFingerprintFragment(R.layout.fragment_activate_fingerprint) {

	private val binding by viewBinding(FragmentActivateFingerprintBinding::bind)

	override fun onFingerprintSuccess() {
		showToast("success")
		// TODO navigate to next screen or go back
	}

	override fun onFingerprintError() {
		showToast("error")
		// TODO navigate to next screen or go back
	}

	override fun initView() {
		super.initView()
		setupToolbar(binding.activateFpToolbar, null, false)

		if (!requireContext().isFingerprintAvailable()) {
			error("You should not suppose to be in ActivateFingerprintFragment when you don´t have fingerprint hardware")
		}

		binding.activateFpBtn.setOnClickListener {
			fingerprintBuilder.invokeFingerPrint()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.menu_fingerprint_activation, menu)
		super.onCreateOptionsMenu(menu, inflater)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.skip -> {
				showToast("skip")
				// TODO navigate to next screen or go back
				return true
			}
		}
		return super.onOptionsItemSelected(item)
	}
}