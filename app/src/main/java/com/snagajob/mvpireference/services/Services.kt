package com.snagajob.mvpireference.services

import android.accounts.NetworkErrorException
import com.snagajob.mvpireference.services.login.LoginService
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Using current pattern for making services available for use app-wide. This pattern is not final.
 */
object Services {

    private val network = Network()

    val loginService: LoginService = LoginService(network.retrofit)

    // Network object encapsulates dependencies ensuring they are not available throughout the application.
    private class Network {
        private val okHttpClient =
                OkHttpClient.Builder()
                        .connectTimeout(7, TimeUnit.SECONDS)
                        .readTimeout(7, TimeUnit.SECONDS)
                        .addInterceptor { chain ->
                            val newRequest = chain.request().newBuilder()

                                    .addHeader(OS_NAME, "Android")
                                    .addHeader(USER_AGENT, "Fake News")
                                    .addHeader(APP_NAME, "PMGo")
                                    .addHeader(APP_VERSION, "0.01.01")
                                    .addHeader(OS_VERSION, "4.3.2")
                                    .addHeader(SESSION_ID, "Fake News")
                                    .addHeader(APP_INSTANCE_ID, "Fake News")
                                    .build()

                            try {
                                chain.proceed(newRequest)
                            } catch (exception: Exception) {
                                throw NetworkErrorException("Exception encountered at endpoint ${chain.request().url()}",
                                        exception)
                            }
                        }
                        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .build()

        private val moshi =
                Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()

        val retrofit: Retrofit =
                Retrofit.Builder()
                        .baseUrl("https://mpi-go.snagqa.com/")
                        .client(okHttpClient)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build()
    }
}