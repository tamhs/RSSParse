package com.pet.rsspaser.data.remote.api.middleware

import com.pet.rsspaser.data.remote.api.exception.ApiException
import com.pet.rsspaser.data.remote.api.exception.DefaultServerErrorData
import com.pet.rsspaser.data.remote.api.exception.ServerErrorData
import com.pet.rsspaser.data.remote.api.exception.UnexpectedException
import com.pet.rsspaser.di.ExceptionsMapper
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.*

class AppCallWithErrorHandling(
    private val retrofit: Retrofit,
    private val delegate: Call<Any>,
) : Call<Any> by delegate {

    override fun clone() = AppCallWithErrorHandling(retrofit, delegate.clone())

    override fun enqueue(callback: Callback<Any>) {
        delegate.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    callback.onResponse(call, response)
                } else {
                    callback.onFailure(call, mapExceptionOfCall(call, HttpException(response)))
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onFailure(call, mapExceptionOfCall(call, t))
            }
        })
    }

    private fun mapExceptionOfCall(call: Call<Any>, t: Throwable): Throwable {
        val retrofitInvocation = call.request().tag(Invocation::class.java)
        val method = retrofitInvocation?.method()
        val mapperAnnotation = method?.getAnnotation(ExceptionsMapper::class.java)
        val mapper = try {
            val defaultConstructor = mapperAnnotation?.value?.java?.constructors?.first()
            defaultConstructor?.newInstance() as HttpExceptionMapper
        } catch (e: Exception) {
            null
        }
        val arguments = retrofitInvocation?.arguments()?.filterNotNull() ?: emptyList()
        return mapToDomainException(t, mapper, arguments, call.request())
    }

    private fun mapToDomainException(
        remoteException: Throwable,
        httpExceptionsMapper: HttpExceptionMapper?,
        callArguments: List<Any>,
        requestInformation: Request,
    ): Throwable {
        if (remoteException !is HttpException) return remoteException

        // Convert by Mapper
        if (httpExceptionsMapper != null) {
            return httpExceptionsMapper.map(remoteException, this, retrofit, callArguments)
        }

        // Parse default
        return parserServerException<DefaultServerErrorData>(
            remoteException,
            retrofit,
            requestInformation
        )
    }

    /**
     * Public to any instance HttpExceptionMapper
     */
    @SuppressWarnings("WeakerAccess")
    inline fun <reified T : ServerErrorData> parserServerException(
        httpException: HttpException,
        retrofit: Retrofit,
        requestInformation: Request,
    ): Exception {
        val errorBody1 = httpException.response()?.errorBody()
        val errorBody = errorBody1 ?: return UnexpectedException(httpException)
        val response = retrofit.parseErrorSafety<T>(errorBody) ?: return UnexpectedException()
        return ApiException(response, requestInformation.url.toString())
    }

    @Suppress("unused")
    @Throws(java.lang.Exception::class)
    inline fun <reified T> Retrofit.parseError(errorBody: ResponseBody): T {
        val converter = this.responseBodyConverter<T>(T::class.java, arrayOf())
        return converter.convert(errorBody)!!
    }

    inline fun <reified T> Retrofit.parseErrorSafety(errorBody: ResponseBody): T? {
        val converter = this.responseBodyConverter<T>(T::class.java, arrayOf())
        return try {
            converter.convert(errorBody)
        } catch (ex: Throwable) {
            ex.printStackTrace()
            null
        }
    }
}