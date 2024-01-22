package com.pet.rsspaser.data.remote.api.middleware

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by tam.hs on 1/22/2024.
 */
class AppErrorsCallAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, Call<*>>? {
        if (getRawType(returnType) != Call::class.java ||
            returnType !is ParameterizedType ||
            returnType.actualTypeArguments.size != 1
        ) {
            return null
        }
        val delegate = retrofit.nextCallAdapter(this, returnType, annotations)
        @Suppress("UNCHECKED_CAST")
        return AppErrorsCallAdapter(
            retrofit,
            delegateAdapter = delegate as CallAdapter<Any, Call<*>>,
        )
    }
}

class AppErrorsCallAdapter(
    private val retrofit: Retrofit,
    private val delegateAdapter: CallAdapter<Any, Call<*>>,
) : CallAdapter<Any, Call<*>> by delegateAdapter {

    override fun adapt(call: Call<Any>): Call<*> {
        return delegateAdapter.adapt(AppCallWithErrorHandling(retrofit, call))
    }
}