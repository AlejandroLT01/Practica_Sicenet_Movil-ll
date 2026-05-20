package com.example.sicenetmultiplatform.data

import com.example.sicenetmultiplatform.data.local.SicenetDatabase
import com.example.sicenetmultiplatform.data.local.getDatabaseBuilder
import com.example.sicenetmultiplatform.data.repository.LocalRepository
import com.example.sicenetmultiplatform.data.repository.SicenetRepository
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object SicenetComponent {
    val httpClient by lazy {
        HttpClient {
            install(HttpCookies)
            install(Logging) {
                level = LogLevel.ALL
                logger = Logger.DEFAULT
            }
            install(ContentNegotiation) {
                json(Json { 
                    ignoreUnknownKeys = true 
                    prettyPrint = true
                })
            }
        }
    }

    val database by lazy {
        SicenetDatabase.getDatabase(getDatabaseBuilder())
    }

    val sicenetRepository by lazy {
        SicenetRepository(SicenetApiService(httpClient))
    }

    val localRepository by lazy {
        LocalRepository(database.sicenetDao())
    }

    val syncEngine by lazy {
        SicenetSyncEngine(localRepository, sicenetRepository)
    }
}
