package com.pet.rsspaser.di

import android.annotation.SuppressLint
import android.app.Application
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.pet.rsspaser.data.remote.api.ApiService
import com.pet.rsspaser.data.remote.api.middleware.AppErrorsCallAdapterFactory
import com.pet.rsspaser.data.remote.api.middleware.ConnectivityInterceptor
import com.pet.rsspaser.data.remote.api.middleware.InterceptorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by tam.hs on 1/22/2024.
 */

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {
    @Singleton
    @Provides
    fun provideOkHttpCache(app: Application): Cache {
        val cacheSize: Long = 10 * 1024 * 1024 // 10MiB
        return Cache(app.cacheDir, cacheSize)
    }

    @SuppressLint("TrustAllX509TrustManager, CustomX509TrustManager")
    @Singleton
    @Provides
    fun providerX509TrustManager(): X509TrustManager {
        // Create a trust manager that does not validate certificate chains
        return object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    }

    @Singleton
    @Provides
    fun providerSslSocketFactory(trust: X509TrustManager): SSLSocketFactory {
        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(trust), java.security.SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        return sslContext.socketFactory
    }

    @Singleton
    @Provides
    fun providerConnectivityInterceptor(app: Application): ConnectivityInterceptor =
        ConnectivityInterceptor(app)

    @RemoteOkHttpClient
    @Singleton
    @Provides
    fun provideOkHttpClient(
        cache: Cache,
        interceptor: Interceptor,
        sslSocketFactory: SSLSocketFactory,
        trustAllCerts: X509TrustManager,
        connectivityInterceptor: ConnectivityInterceptor
    ): OkHttpClient {
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.cache(cache)
        httpClientBuilder.addInterceptor(interceptor)

        httpClientBuilder.readTimeout(
            READ_TIMEOUT, TimeUnit.SECONDS
        )
        httpClientBuilder.writeTimeout(
            WRITE_TIMEOUT, TimeUnit.SECONDS
        )
        httpClientBuilder.connectTimeout(
            CONNECTION_TIMEOUT, TimeUnit.SECONDS
        )
        httpClientBuilder.sslSocketFactory(sslSocketFactory, trustAllCerts)
        httpClientBuilder.addInterceptor(connectivityInterceptor)
        httpClientBuilder.hostnameVerifier { _, _ -> true }

        /*if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            httpClientBuilder.addInterceptor(logging)
            logging.level = HttpLoggingInterceptor.Level.BODY
        }*/

        return httpClientBuilder.build()
    }

    @Singleton
    @Provides
    fun provideInterceptor(): Interceptor {
        return InterceptorImpl()
    }

    @Singleton
    @Provides
    @OptIn(ExperimentalSerializationApi::class)
    fun createTemplateRetrofit(
        @RemoteOkHttpClient okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("BuildConfig.END_POINT")
            .addConverterFactory(json.asConverterFactory(contentType))
            .addCallAdapterFactory(AppErrorsCallAdapterFactory())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideApi(json: Json, @RemoteOkHttpClient okHttpClient: OkHttpClient): ApiService {
        return createTemplateRetrofit(okHttpClient, json).create(ApiService::class.java)
    }

    private const val READ_TIMEOUT: Long = 60
    private const val WRITE_TIMEOUT: Long = 60
    private const val CONNECTION_TIMEOUT: Long = 60
}