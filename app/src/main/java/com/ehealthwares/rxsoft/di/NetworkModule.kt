package com.rxsoft.mobile.di

import com.rxsoft.mobile.data.remote.api.*
import com.rxsoft.mobile.data.remote.dto.BigDecimalAdapter
import com.rxsoft.mobile.data.remote.dto.ListResponseAdapterFactory
import com.rxsoft.mobile.data.remote.interceptor.AuthInterceptor
import com.rxsoft.mobile.data.remote.interceptor.ServerUrlInterceptor
import com.rxsoft.mobile.data.remote.interceptor.TokenRefreshInterceptor
import com.rxsoft.mobile.data.remote.interceptor.TraceLoggingInterceptor
import com.rxsoft.mobile.util.ServerUrlManager
import com.rxsoft.mobile.util.TokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        serverUrlInterceptor: ServerUrlInterceptor,
        authInterceptor: AuthInterceptor,
        tokenRefreshInterceptor: TokenRefreshInterceptor,
        traceLoggingInterceptor: TraceLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(serverUrlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(tokenRefreshInterceptor)
            .addInterceptor(traceLoggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(BigDecimalAdapter())
            .add(ListResponseAdapterFactory())
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, serverUrlManager: ServerUrlManager, moshi: Moshi): Retrofit {
        val baseUrl = serverUrlManager.getUrl().let { if (it.endsWith("/")) it else "$it/" }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideSalesApi(retrofit: Retrofit): SalesApi = retrofit.create(SalesApi::class.java)

    @Provides
    @Singleton
    fun provideItemsApi(retrofit: Retrofit): ItemsApi = retrofit.create(ItemsApi::class.java)

    @Provides
    @Singleton
    fun provideCustomersApi(retrofit: Retrofit): CustomersApi = retrofit.create(CustomersApi::class.java)

    @Provides
    @Singleton
    fun provideInventoryApi(retrofit: Retrofit): InventoryApi = retrofit.create(InventoryApi::class.java)

    @Provides
    @Singleton
    fun provideReportsApi(retrofit: Retrofit): ReportsApi = retrofit.create(ReportsApi::class.java)

    @Provides
    @Singleton
    fun providePaymentMethodsApi(retrofit: Retrofit): PaymentMethodsApi = retrofit.create(PaymentMethodsApi::class.java)

    @Provides
    @Singleton
    fun provideConfigApi(retrofit: Retrofit): ConfigApi = retrofit.create(ConfigApi::class.java)

    @Provides
    @Singleton
    fun providePricingApi(retrofit: Retrofit): PricingApi = retrofit.create(PricingApi::class.java)

    @Provides
    @Singleton
    fun provideUploadApi(retrofit: Retrofit): UploadApi = retrofit.create(UploadApi::class.java)

    @Provides
    @Singleton
    fun provideWebsiteApi(retrofit: Retrofit): WebsiteApi = retrofit.create(WebsiteApi::class.java)

    @Provides
    @Singleton
    fun provideGenericProductsApi(retrofit: Retrofit): GenericProductsApi = retrofit.create(GenericProductsApi::class.java)

    @Provides
    @Singleton
    fun providePurchasesApi(retrofit: Retrofit): PurchasesApi = retrofit.create(PurchasesApi::class.java)

    @Provides
    @Singleton
    fun provideSyncApi(retrofit: Retrofit): SyncApi = retrofit.create(SyncApi::class.java)

    @Provides
    @Singleton
    fun providePaymentsApi(retrofit: Retrofit): PaymentsApi = retrofit.create(PaymentsApi::class.java)

    @Provides
    @Singleton
    fun provideChatApi(retrofit: Retrofit): ChatApi = retrofit.create(ChatApi::class.java)

    @Provides
    @Singleton
    fun provideServerUrlInterceptor(serverUrlManager: ServerUrlManager): ServerUrlInterceptor {
        return ServerUrlInterceptor(serverUrlManager)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    fun provideTraceLoggingInterceptor(): TraceLoggingInterceptor {
        return TraceLoggingInterceptor()
    }
}
