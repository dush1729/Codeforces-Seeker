package com.example.cfseeker.data.repository

import com.example.cfseeker.data.local.DatabaseService
import com.example.cfseeker.data.remote.api.NetworkService
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: NetworkService,
    private val db: DatabaseService
) {


}