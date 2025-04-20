package com.durranitech.realtimechatapp.data.utils
sealed class Resource<out T> {
    class Loading<T> : Resource<T>()
    class Success<T>(val data: T) : Resource<T>()
    class Error<T>(val message: String) : Resource<T>()
    class Idle<T> : Resource<T>()
}