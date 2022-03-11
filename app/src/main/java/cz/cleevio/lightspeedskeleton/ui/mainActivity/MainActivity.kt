package cz.cleevio.lightspeedskeleton.ui.mainActivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cz.cleevio.lightspeedskeleton.R
import cz.cleevio.lightspeedskeleton.databinding.ActivityMainBinding
import cz.cleevio.network.NetworkError
import kotlinx.coroutines.launch
import lightbase.core.extensions.showSnackbar
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding
	private val errorFlow: NetworkError by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		window.setBackgroundDrawableResource(R.color.background)
		// Going to Edge to Edge
		WindowCompat.setDecorFitsSystemWindows(window, false)

		bindObservers()
	}

	private fun bindObservers() {
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				errorFlow.error.collect { error ->
					error?.message?.let { messageCode ->
						if (messageCode != -1) {
							showSnackbar(
								view = binding.container,
								message = getString(messageCode)
							)
						}
					}
				}
			}
		}
	}
}