package com.pet.rsspaser.di

import com.pet.rsspaser.data.remote.api.middleware.HttpExceptionMapper
import javax.inject.Qualifier
import kotlin.reflect.KClass

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteOkHttpClient


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IODispatcher

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ExceptionsMapper(val value: KClass<out HttpExceptionMapper>)