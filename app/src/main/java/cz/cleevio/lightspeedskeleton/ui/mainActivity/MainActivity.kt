package cz.cleevio.lightspeedskeleton.ui.mainActivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import cz.cleevio.lightspeedskeleton.R

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		window.setBackgroundDrawableResource(R.color.background)
		// Going to Edge to Edge
		WindowCompat.setDecorFitsSystemWindows(window, false)
	}
}