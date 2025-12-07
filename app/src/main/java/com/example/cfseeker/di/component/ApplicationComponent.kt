package com.example.cfseeker.di.component

import com.example.cfseeker.MyApplication
import com.example.cfseeker.data.local.DatabaseService
import com.example.cfseeker.data.remote.api.NetworkService
import com.example.cfseeker.di.module.ApplicationModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: MyApplication)

    fun getNetworkService(): NetworkService
    fun getDatabaseService(): DatabaseService
}