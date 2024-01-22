package com.pet.rsspaser.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.pet.rsspaser.MainViewModel
import com.pet.rsspaser.data.remote.api.ApiService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by tam.hs on 1/22/2024.
 */
val viewModelModule = module {
    viewModel { MainViewModel() }
}

val repositoryModule = module {
    /*
    single {
        BookRepository(get())
    }
     */
}

val apiModule = module {
    fun provideUseApi(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    single { provideUseApi(get()) }
}

val retrofitModule = module {

    fun provideHttpClient(): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
        return okHttpClientBuilder.build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun provideRetrofit(factory: Json, client: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(factory.asConverterFactory(contentType))
            //.addCallAdapterFactory(AppErrorsCallAdapterFactory())
            .client(client)
            .build()
    }
    single { provideHttpClient() }
    single { provideRetrofit(get(), get()) }
}