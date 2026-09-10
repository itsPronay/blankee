package com.pronaycoding.blankee.core.service.crash

/** No-op crash reporter — the fdroid flavor ships with no analytics or crash reporting. */
class FirebaseCrashReporter : CrashReporter {
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}
