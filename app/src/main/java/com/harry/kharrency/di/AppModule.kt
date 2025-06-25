package com.harry.kharrency.di

import android.content.Context
import com.harry.kharrency.ui.theme.ThemeManager
import com.harry.repository.UpdateService
import com.harry.viewmodels.UpdateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context
    ): ThemeManager {
        return ThemeManager(context)
    }
    
    @Provides
    @Singleton
    fun provideUpdateService(): UpdateService {
        return UpdateService()
    }
    
    @Provides
    @Singleton
    fun provideUpdateManager(
        @ApplicationContext context: Context,
        updateService: UpdateService
    ): UpdateManager {
        return UpdateManager(context, updateService)
    }
} 