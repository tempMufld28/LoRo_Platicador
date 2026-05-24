package com.lockchat.app.di

import com.lockchat.app.data.local.IdentityDataStore
import com.lockchat.app.data.repository.ContactoRepositoryImpl
import com.lockchat.app.data.repository.MensajeRepositoryImpl
import com.lockchat.app.domain.repository.ContactRepository
import com.lockchat.app.domain.repository.IdentityRepository
import com.lockchat.app.domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt que vincula interfaces de Repository.
 *
 * Stubs eliminados completamente — todo apunta a implementaciones Room/DataStore.
 * AuthRepository, ConversationRepository, NodeRepository, PingRepository:
 *   manejados directamente por TransportManager y ViewModels.
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindIdentityRepository(
        impl: IdentityDataStore
    ): IdentityRepository

    @Binds @Singleton
    abstract fun bindContactRepository(
        impl: ContactoRepositoryImpl
    ): ContactRepository

    @Binds @Singleton
    abstract fun bindMessageRepository(
        impl: MensajeRepositoryImpl
    ): MessageRepository
}
