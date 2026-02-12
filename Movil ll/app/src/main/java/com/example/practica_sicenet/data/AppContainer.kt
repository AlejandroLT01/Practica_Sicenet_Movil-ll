package com.example.practica_sicenet.data

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val sicenetRepository: SicenetRepository
}

/**
 * Implementation for the Dependency Injection container.
 */
class DefaultAppContainer : AppContainer {
    override val sicenetRepository: SicenetRepository by lazy {
        NetworkSicenetRepository()
    }
}