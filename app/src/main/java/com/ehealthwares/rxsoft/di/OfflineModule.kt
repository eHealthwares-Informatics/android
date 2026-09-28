package com.ehealthwares.rxsoft.di

import android.content.Context
import androidx.room.Room
import com.ehealthwares.rxsoft.data.local.CachedChatMessageDao
import com.ehealthwares.rxsoft.data.local.CachedConversationDao
import com.ehealthwares.rxsoft.data.local.CategoryDao
import com.ehealthwares.rxsoft.data.local.CustomerDao
import com.ehealthwares.rxsoft.data.local.MIGRATION_2_3
import com.ehealthwares.rxsoft.data.local.MIGRATION_3_4
import com.ehealthwares.rxsoft.data.local.MIGRATION_4_5
import com.ehealthwares.rxsoft.data.local.MIGRATION_5_6
import com.ehealthwares.rxsoft.data.local.OfflineDatabase
import com.ehealthwares.rxsoft.data.local.OfflineItemDao
import com.ehealthwares.rxsoft.data.local.PaymentMethodDao
import com.ehealthwares.rxsoft.data.local.PendingOrderDao
import com.ehealthwares.rxsoft.data.local.PendingSaleDao
import com.ehealthwares.rxsoft.data.local.PendingStockAdjustmentDao
import com.ehealthwares.rxsoft.data.local.PriceDao
import com.ehealthwares.rxsoft.data.local.PriceListDao
import com.ehealthwares.rxsoft.data.local.StockBalanceDao
import com.ehealthwares.rxsoft.data.local.StockLocationDao
import com.ehealthwares.rxsoft.data.local.SyncStateDao
import com.ehealthwares.rxsoft.data.local.UomDao
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
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
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

    @Provides
    fun providePaymentMethodDao(db: OfflineDatabase): PaymentMethodDao = db.paymentMethodDao()

    @Provides
    fun providePendingStockAdjustmentDao(db: OfflineDatabase): PendingStockAdjustmentDao = db.pendingStockAdjustmentDao()

    @Provides
    fun providePendingSaleDao(db: OfflineDatabase): PendingSaleDao = db.pendingSaleDao()
}
