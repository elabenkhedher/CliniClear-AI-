package com.example.cliniclearai.common

/**
 * A generic class that holds a value with its loading status.
 * Suitable for Clean Architecture data flows and Offline-First caching.
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val cause: Throwable? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null, cause: Throwable? = null) : Resource<T>(data, message, cause)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
