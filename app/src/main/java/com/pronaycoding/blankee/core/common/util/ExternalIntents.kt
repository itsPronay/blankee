package com.pronaycoding.blankee.core.common.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.pronaycoding.blankee.core.common.Constants
import com.pronaycoding.blankee.R

/**
 * Opens an external URL in the device's default browser.
 *
 * This utility function safely handles opening URLs with graceful error handling:
 * - If a browser is not available: Shows a "No browser installed" toast
 * - On other exceptions: Shows a generic error toast
 *
 * The function catches [ActivityNotFoundException] separately to provide specific user feedback
 * when no browser app is available on the device.
 *
 * Usage example:
 * ```kotlin
 * openExternalUrl(context, "https://github.com/itsPronay/blankee")
 * ```
 *
 * @param context The context used to start the activity and show toasts
 * @param url The full URL to open (should start with http:// or https://)
 */
fun openExternalUrl(
    context: Context,
    url: String,
) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast
            .makeText(
                context,
                context.getString(R.string.no_browser_installed),
                Toast.LENGTH_LONG,
            ).show()
    } catch (e: Exception) {
        Toast
            .makeText(
                context,
                context.getString(R.string.error_unexpected),
                Toast.LENGTH_LONG,
            ).show()
    }
}

/**
 * Opens the system share sheet with a prefilled app recommendation message.
 */
fun shareApp(
    context: Context,
) {
    val shareMessage =
        context.getString(
            R.string.share_app_message,
            Constants.PLAY_STORE_URL,
        )

    val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }

    try {
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                context.getString(R.string.share_app_chooser_title),
            ),
        )
    } catch (e: ActivityNotFoundException) {
        Toast
            .makeText(
                context,
                context.getString(R.string.no_share_app_installed),
                Toast.LENGTH_LONG,
            ).show()
    } catch (e: Exception) {
        Toast
            .makeText(
                context,
                context.getString(R.string.error_unexpected),
                Toast.LENGTH_LONG,
            ).show()
    }
}
