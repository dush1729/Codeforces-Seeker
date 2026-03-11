package com.dush1729.cfseeker.data.remote.api

import io.ktor.client.HttpClient

expect fun createPlatformHttpClient(): HttpClient
