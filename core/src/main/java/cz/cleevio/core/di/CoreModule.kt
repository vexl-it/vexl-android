package cz.cleevio.core.di

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.util.CoilUtils
import cz.cleevio.core.R
import cz.cleevio.core.utils.LocationHelper
import cz.cleevio.core.utils.NavMainGraphModel
import cz.cleevio.core.utils.UserUtils
import cz.cleevio.core.utils.marketGraph.MarketChartUtils
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.LastLineSpacingSpan
import lightbase.camera.utils.ImageHelper
import okhttp3.OkHttpClient
import org.commonmark.node.ListItem
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {

	single {
		ImageLoader.Builder(androidContext())
			.crossfade(true)
			.okHttpClient {
				OkHttpClient.Builder()
					.cache(CoilUtils.createDefaultCache(androidContext()))
					.build()
			}
			.build()
	}

	single {
		ImageHelper()
	}

	single {
		UserUtils(
			encryptedPreferenceRepository = get()
		)
	}

	single {
		NavMainGraphModel()
	}

	single {
		LocationHelper(
			moshi = get()
		)
	}

	single {
		MarketChartUtils(
			androidContext()
		)
	}

	single {
		provideMarkwon(get())
	}
}

private fun provideMarkwon(context: Context) = Markwon.builder(context)
	.usePlugin(object : AbstractMarkwonPlugin() {
		override fun configureTheme(builder: MarkwonTheme.Builder) {
			val primaryColor = ContextCompat.getColor(context, R.color.white)
			builder
				.headingBreakHeight(0)
				.headingTypeface(Typeface.DEFAULT)
				.headingTextSizeMultipliers(floatArrayOf(1.2f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f))
				.listItemColor(primaryColor)
				.linkColor(primaryColor)
				.bulletWidth(25)
		}

		override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
			builder.appendFactory(
				ListItem::class.java
			) { _, _ ->
				LastLineSpacingSpan(
					// Spacing for bottom of paragraph markdown
					40
				)
			}
		}
	}).build()