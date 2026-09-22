package com.rxsoft.mobile.di

import android.content.Context
import androidx.room.Room
import com.rxsoft.mobile.data.local.CachedChatMessageDao
import com.rxsoft.mobile.data.local.CachedConversationDao
import com.rxsoft.mobile.data.local.CategoryDao
import com.rxsoft.mobile.data.local.CustomerDao
import com.rxsoft.mobile.data.local.MIGRATION_2_3
import com.rxsoft.mobile.data.local.MIGRATION_3_4
import com.rxsoft.mobile.data.local.OfflineDatabase
import com.rxsoft.mobile.data.local.OfflineItemDao
import com.rxsoft.mobile.data.local.PendingOrderDao
import com.rxsoft.mobile.data.local.PriceDao
import com.rxsoft.mobile.data.local.PriceListDao
import com.rxsoft.mobile.data.local.StockBalanceDao
import com.rxsoft.mobile.data.local.StockLocationDao
import com.rxsoft.mobile.data.local.SyncStateDao
import com.rxsoft.mobile.data.local.UomDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OfflineModule {

    @Provides
    @Singleton
    fun provideOfflineDatabase(@ApplicationContext context: Context): OfflineDatabase {
        return Room.databaseBuilder(context, OfflineDatabase::class.java, "rxsoft_offline.db")
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideOfflineItemDao(db: OfflineDatabase): OfflineItemDao = db.offlineItemDao()

    @Provides
    fun providePendingOrderDao(db: OfflineDatabase): PendingOrderDao = db.pendingOrderDao()

    @Provides
    fun providePriceListDao(db: OfflineDatabase): PriceListDao = db.priceListDao()

    @Provides
    fun providePriceDao(db: OfflineDatabase): PriceDao = db.priceDao()

    @Provides
    fun provideStockLocationDao(db: OfflineDatabase): StockLocationDao = db.stockLocationDao()

    @Provides
    fun provideStockBalanceDao(db: OfflineDatabase): StockBalanceDao = db.stockBalanceDao()

    @Provides
    fun provideCustomerDao(db: OfflineDatabase): CustomerDao = db.customerDao()

    @Provides
    fun provideCategoryDao(db: OfflineDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideUomDao(db: OfflineDatabase): UomDao = db.uomDao()

    @Provides
    fun provideSyncStateDao(db: OfflineDatabase): SyncStateDao = db.syncStateDao()

    @Provides
    fun provideCachedConversationDao(db: OfflineDatabase): CachedConversationDao = db.cachedConversationDao()

    @Provides
    fun provideCachedChatMessageDao(db: OfflineDatabase): CachedChatMessageDao = db.cachedChatMessageDao()
}
