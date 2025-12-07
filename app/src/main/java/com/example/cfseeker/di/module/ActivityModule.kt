package com.example.cfseeker.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.cfseeker.data.repository.UserRepository
import com.example.cfseeker.di.ActivityScope
import com.example.cfseeker.ui.UserViewModel
import com.example.cfseeker.ui.base.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(private val activity: AppCompatActivity) {
    @ActivityScope
    @Provides
    fun provideUserViewModel(userRepository: UserRepository): UserViewModel {
        val factory = ViewModelProviderFactory { UserViewModel(userRepository) }
        return ViewModelProvider(activity, factory)[UserViewModel::class.java]
    }
}