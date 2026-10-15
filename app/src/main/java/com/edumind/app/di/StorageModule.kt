package com.edumind.app.di

import com.edumind.app.data.local.prefs.EncryptedTokenStorage
import com.edumind.app.data.local.prefs.TokenStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindTokenStorage(
        encryptedTokenStorage: EncryptedTokenStorage
    ): TokenStorage
}
