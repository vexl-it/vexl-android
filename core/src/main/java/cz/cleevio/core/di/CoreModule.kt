package cz.cleevio.core.di

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.util.CoilUtils
import cz.cleevio.core.R
import cz.cleevio.core.utils.*
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

const val SIZE_OF_HEADERS = 1.2f
const val SIZE_OF_BULLETS = 25
const val SIZE_OF_HEADING_BREAK = 0
const val LINE_SPACING = 40

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
		BackgroundQueue(
			offerRepository = get(),
			contactRepository = get(),
			encryptedPreferenceRepository = get(),
			offerUtils = get()
		)
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
		NotificationUtils(
			androidContext()
		)
	}

	single {
		provideMarkwon(get())
	}

	single {
		OfferUtils(
			moshi = get(),
		)
	}

	single {
		EncryptionUtils()
	}

	single {
		TempUtils(
			userRepository = get(),
			encryptedPreferenceRepository = get()
		)
	}
}

private fun provideMarkwon(context: Context) = Markwon.builder(context)
	.usePlugin(object : AbstractMarkwonPlugin() {
		override fun configureTheme(builder: MarkwonTheme.Builder) {
			val primaryColor = ContextCompat.getColor(context, R.color.white)
			builder
				.headingBreakHeight(SIZE_OF_HEADING_BREAK)
				.headingTypeface(Typeface.DEFAULT)
				.headingTextSizeMultipliers(
					floatArrayOf(SIZE_OF_HEADERS, SIZE_OF_HEADERS, SIZE_OF_HEADERS, SIZE_OF_HEADERS, SIZE_OF_HEADERS, SIZE_OF_HEADERS)
				)
				.listItemColor(primaryColor)
				.linkColor(primaryColor)
				.bulletWidth(SIZE_OF_BULLETS)
		}

		override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
			builder.appendFactory(
				ListItem::class.java
			) { _, _ ->
				LastLineSpacingSpan(
					// Spacing for bottom of paragraph markdown
					LINE_SPACING
				)
			}
		}
	}).build()