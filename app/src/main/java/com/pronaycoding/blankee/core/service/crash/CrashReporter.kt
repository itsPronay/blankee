package com.pronaycoding.blankee.core.service.crash

/**
 * Abstraction over crash reporting so the `main` source set stays free of
 * proprietary Google code. The `gplay` flavor backs this with Firebase
 * Crashlytics; the `fdroid` flavor uses a no-op implementation.
 */
interface CrashReporter {
    fun setCollectionEnabled(enabled: Boolean)
}
