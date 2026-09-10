package com.pronaycoding.blankee

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Blankee Application class.
 *
 * This is the entry point for the entire application. It initializes:
 * - Koin dependency injection framework with all modules
 *
 * Extends [Application] to provide application-level lifecycle methods.
 * Called once when the app process is created.
 *
 * @see KoinModule for dependency injection configuration
 */
class App : Application() {
    /**
     * Called when the application is starting.
     *
     * This method:
        * 1. Starts Koin with all configured modules
     *
     * Debug logging for Koin is enabled in debug builds to help diagnose DI issues.
     */
    override fun onCreate() {
        super.onCreate()

        // Start Koin dependency injection framework
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger(Level.DEBUG) // Enable debug logging only in debug builds
            }
            androidContext(this@App)
            modules(KoinModule.allModules)
        }
    }
}
