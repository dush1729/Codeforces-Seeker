package com.example.cfseeker.di.component

import com.example.cfseeker.MainActivity
import com.example.cfseeker.di.ActivityScope
import com.example.cfseeker.di.module.ActivityModule
import dagger.Component

@ActivityScope
@Component(modules = [ActivityModule::class], dependencies = [ApplicationComponent::class])
interface ActivityComponent {
    fun inject(activity: MainActivity)
}