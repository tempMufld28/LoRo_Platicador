package com.lockchat.app.di

import android.content.Context
import androidx.room.Room
import com.lockchat.app.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para dependencias de infraestructura.
 *
 * Provee: Room DB, DAOs.
 * SharedPreferences eliminado — reemplazado por IdentityDataStore (DataStore).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lockchat.db"
        )
        .fallbackToDestructiveMigration() // dev: OK; producción: usar Migration
        .build()

    @Provides
    @Singleton
    fun provideContactoDao(db: AppDatabase) = db.contactoDao()

    @Provides
    @Singleton
    fun provideMensajeDao(db: AppDatabase) = db.mensajeDao()

    @Provides
    @Singleton
    fun provideSolicitudDao(db: AppDatabase) = db.solicitudDao()
}
