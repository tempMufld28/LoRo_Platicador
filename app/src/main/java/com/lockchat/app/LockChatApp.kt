package com.lockchat.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Lock-Chat Application class.
 *
 * @HiltAndroidApp dispara la generación de código de Hilt.
 * También configura WorkManager para usar HiltWorkerFactory,
 * permitiendo inyección de dependencias en Workers (OutboxRetryWorker, etc.).
 */
@HiltAndroidApp
class LockChatApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Fase 2: inicializar Keystore, SQLCipher, etc.
    }
}
