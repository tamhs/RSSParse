package com.pet.rsspaser.data.remote.api.middleware

import retrofit2.HttpException
import retrofit2.Retrofit

interface HttpExceptionMapper {
    fun map(
        httpException: HttpException,
        handler: AppCallWithErrorHandling,
        retrofit: Retrofit,
        callArguments: List<Any>
    ): Exception
}