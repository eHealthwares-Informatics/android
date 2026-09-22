package com.rxsoft.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedItemEntity>)

    @Query("SELECT * FROM cached_items WHERE searchText LIKE '%' || :needle || '%' ORDER BY name LIMIT :limit")
    suspend fun search(needle: String, limit: Int = 30): List<CachedItemEntity>

    @Query("SELECT * FROM cached_items WHERE itemId = :itemId LIMIT 1")
    suspend fun getById(itemId: String): CachedItemEntity?

    @Query(
        "SELECT i.*, p.unitPrice AS unitPrice FROM cached_items i " +
            "LEFT JOIN cached_prices p ON p.itemId = i.itemId AND p.priceListId = :priceListId " +
            "ORDER BY i.name",
    )
    suspend fun catalog(priceListId: String?): List<CatalogItemWithPrice>

    @Query(
        "SELECT i.*, p.unitPrice AS unitPrice FROM cached_items i " +
            "LEFT JOIN cached_prices p ON p.itemId = i.itemId AND p.priceListId = :priceListId " +
            "ORDER BY i.name",
    )
    fun observeCatalog(priceListId: String?): Flow<List<CatalogItemWithPrice>>

    @Query("SELECT COUNT(*) FROM cached_items")
    suspend fun count(): Int

    @Query("DELETE FROM cached_items WHERE itemId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM cached_items")
    suspend fun clear()
}

@Dao
interface PriceListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedPriceListEntity>)

    @Query("SELECT * FROM cached_price_lists ORDER BY name")
    suspend fun all(): List<CachedPriceListEntity>

    @Query("SELECT COUNT(*) FROM cached_price_lists")
    suspend fun count(): Int

    @Query("DELETE FROM cached_price_lists WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM cached_price_lists")
    suspend fun clear()
}

@Dao
interface PriceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedPriceEntity>)

    @Query("SELECT unitPrice FROM cached_prices WHERE priceListId = :priceListId AND itemId = :itemId LIMIT 1")
    suspend fun unitPrice(priceListId: String, itemId: String): String?

    @Query("SELECT COUNT(*) FROM cached_prices")
    suspend fun count(): Int

    @Query("DELETE FROM cached_prices WHERE priceListId = :priceListId AND itemId = :itemId")
    suspend fun delete(priceListId: String, itemId: String)

    @Query("DELETE FROM cached_prices")
    suspend fun clear()
}

@Dao
interface StockLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedStockLocationEntity>)

    @Query("SELECT * FROM cached_stock_locations ORDER BY name")
    suspend fun all(): List<CachedStockLocationEntity>

    @Query("SELECT * FROM cached_stock_locations WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CachedStockLocationEntity?

    @Query("SELECT COUNT(*) FROM cached_stock_locations")
    suspend fun count(): Int

    @Query("DELETE FROM cached_stock_locations WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM cached_stock_locations")
    suspend fun clear()
}

@Dao
interface StockBalanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedStockBalanceEntity>)

    @Query("SELECT * FROM cached_stock_balances WHERE itemId = :itemId")
    suspend fun forItem(itemId: String): List<CachedStockBalanceEntity>

    @Query("SELECT COUNT(*) FROM cached_stock_balances")
    suspend fun count(): Int

    @Query("DELETE FROM cached_stock_balances WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM cached_stock_balances")
    suspend fun clear()
}

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedCustomerEntity>)

    @Query("SELECT * FROM cached_customers WHERE searchText LIKE '%' || :needle || '%' ORDER BY name LIMIT :limit")
    suspend fun search(needle: String, limit: Int = 30): List<CachedCustomerEntity>

    @Query("SELECT COUNT(*) FROM cached_customers")
    suspend fun count(): Int

    @Query("DELETE FROM cached_customers WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM cached_customers")
    suspend fun clear()
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedCategoryEntity>)

    @Query("SELECT * FROM cached_categories ORDER BY name")
    suspend fun all(): List<CachedCategoryEntity>

    @Query("SELECT COUNT(*) FROM cached_categories")
    suspend fun count(): Int

    @Query("DELETE FROM cached_categories WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM cached_categories")
    suspend fun clear()
}

@Dao
interface UomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedUomEntity>)

    @Query("SELECT * FROM cached_uoms ORDER BY name")
    suspend fun all(): List<CachedUomEntity>

    @Query("SELECT COUNT(*) FROM cached_uoms")
    suspend fun count(): Int

    @Query("DELETE FROM cached_uoms WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM cached_uoms")
    suspend fun clear()
}

@Dao
interface SyncStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(state: SyncStateEntity)

    @Query("SELECT * FROM sync_state WHERE entity = :entity LIMIT 1")
    suspend fun get(entity: String): SyncStateEntity?

    @Query("SELECT * FROM sync_state")
    suspend fun all(): List<SyncStateEntity>

    @Query("SELECT * FROM sync_state")
    fun observeAll(): Flow<List<SyncStateEntity>>
}

@Dao
interface CachedConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedConversationEntity>)

    @Query("SELECT * FROM cached_conversations ORDER BY lastMessageAt IS NULL, lastMessageAt DESC")
    fun observeAll(): Flow<List<CachedConversationEntity>>

    @Query("SELECT * FROM cached_conversations ORDER BY lastMessageAt IS NULL, lastMessageAt DESC")
    suspend fun all(): List<CachedConversationEntity>

    @Query("SELECT * FROM cached_conversations WHERE conversationId = :conversationId LIMIT 1")
    suspend fun getById(conversationId: String): CachedConversationEntity?

    @Query("SELECT COUNT(*) FROM cached_conversations")
    suspend fun count(): Int

    /** A locally-observed message on a conversation the user is not viewing. */
    @Query("UPDATE cached_conversations SET unreadDelta = unreadDelta + 1 WHERE conversationId = :conversationId")
    suspend fun incrementUnread(conversationId: String)

    /** The user opened the conversation: clear all unread state. */
    @Query("UPDATE cached_conversations SET unreadDelta = 0, unreadCount = 0 WHERE conversationId = :conversationId")
    suspend fun markRead(conversationId: String)

    @Query("DELETE FROM cached_conversations WHERE conversationId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM cached_conversations")
    suspend fun clear()
}

@Dao
interface CachedChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedChatMessageEntity>)

    @Query("SELECT * FROM cached_chat_messages WHERE conversationId = :conversationId ORDER BY createdAt ASC, id ASC")
    fun observeConversation(conversationId: String): Flow<List<CachedChatMessageEntity>>

    @Query("SELECT * FROM cached_chat_messages WHERE conversationId = :conversationId ORDER BY createdAt ASC, id ASC")
    suspend fun forConversation(conversationId: String): List<CachedChatMessageEntity>

    @Query("DELETE FROM cached_chat_messages WHERE conversationId IN (:ids)")
    suspend fun deleteByConversationIds(ids: List<String>)

    @Query("DELETE FROM cached_chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteByConversationId(conversationId: String)

    @Query("DELETE FROM cached_chat_messages")
    suspend fun clear()
}

@Dao
interface PendingOrderDao {
    @Insert
    suspend fun insert(order: PendingOrderEntity): Long

    @Query("SELECT * FROM pending_orders ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<PendingOrderEntity>>

    @Query("SELECT * FROM pending_orders ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingOrderEntity>

    @Query("SELECT COUNT(*) FROM pending_orders")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM pending_orders WHERE clientRef = :clientRef")
    suspend fun deleteByClientRef(clientRef: String)

    @Query("UPDATE pending_orders SET pushAttempts = pushAttempts + 1, lastError = :error WHERE clientRef = :clientRef")
    suspend fun markPushFailed(clientRef: String, error: String)
}

@androidx.room.Database(
    entities = [
        CachedItemEntity::class,
        CachedPriceListEntity::class,
        CachedPriceEntity::class,
        CachedStockLocationEntity::class,
        CachedStockBalanceEntity::class,
        CachedCustomerEntity::class,
        CachedCategoryEntity::class,
        CachedUomEntity::class,
        SyncStateEntity::class,
        PendingOrderEntity::class,
        CachedConversationEntity::class,
        CachedChatMessageEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class OfflineDatabase : androidx.room.RoomDatabase() {
    abstract fun offlineItemDao(): OfflineItemDao
    abstract fun priceListDao(): PriceListDao
    abstract fun priceDao(): PriceDao
    abstract fun stockLocationDao(): StockLocationDao
    abstract fun stockBalanceDao(): StockBalanceDao
    abstract fun customerDao(): CustomerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun uomDao(): UomDao
    abstract fun syncStateDao(): SyncStateDao
    abstract fun pendingOrderDao(): PendingOrderDao
    abstract fun cachedConversationDao(): CachedConversationDao
    abstract fun cachedChatMessageDao(): CachedChatMessageDao
}

/**
 * v2 → v3: expand the offline catalog cache (items/prices/stock/customers/etc.).
 * `cached_items` is dropped and recreated (it is a rebuildable cache) while the
 * `pending_orders` outbox is preserved.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `cached_items`")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_items` (" +
                "`itemId` TEXT NOT NULL, `orgItemId` TEXT, `name` TEXT NOT NULL, `displayName` TEXT, " +
                "`code` TEXT, `barcode` TEXT, `categoryId` TEXT, `categoryCode` TEXT, `categoryName` TEXT, " +
                "`baseUomId` TEXT, `baseUomCode` TEXT, `baseUomName` TEXT, `saleUomId` TEXT, `saleUomCode` TEXT, " +
                "`saleUomName` TEXT, `genericProductCode` TEXT, `genericProductName` TEXT, `imageUrl` TEXT, " +
                "`smallImageUrl` TEXT, `updatedAt` TEXT, `searchText` TEXT NOT NULL, `cachedAt` INTEGER NOT NULL, " +
                "PRIMARY KEY(`itemId`))",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_items_name` ON `cached_items` (`name`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_items_code` ON `cached_items` (`code`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_items_barcode` ON `cached_items` (`barcode`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_items_searchText` ON `cached_items` (`searchText`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_price_lists` (`id` TEXT NOT NULL, `code` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, `isDefault` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `updatedAt` TEXT, " +
                "PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_prices` (`priceListId` TEXT NOT NULL, `itemId` TEXT NOT NULL, " +
                "`unitPrice` TEXT NOT NULL, `currencyCode` TEXT, `startsAt` TEXT, `endsAt` TEXT, `updatedAt` TEXT, " +
                "PRIMARY KEY(`priceListId`, `itemId`))",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_prices_itemId` ON `cached_prices` (`itemId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_prices_priceListId` ON `cached_prices` (`priceListId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_stock_locations` (`id` TEXT NOT NULL, `code` TEXT, " +
                "`name` TEXT NOT NULL, `locationType` TEXT, `isActive` INTEGER NOT NULL, `parentId` TEXT, " +
                "`updatedAt` TEXT, PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_stock_balances` (`id` TEXT NOT NULL, `itemId` TEXT, `itemCode` TEXT, " +
                "`itemName` TEXT, `locationId` TEXT, `locationName` TEXT, `lotId` TEXT, `lotCode` TEXT, " +
                "`quantityOnHand` TEXT NOT NULL, `quantityReserved` TEXT NOT NULL, `averageCost` TEXT, " +
                "`reorderMinQty` TEXT, `reorderMaxQty` TEXT, `updatedAt` TEXT, PRIMARY KEY(`id`))",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_stock_balances_itemId` ON `cached_stock_balances` (`itemId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_stock_balances_locationId` ON `cached_stock_balances` (`locationId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_customers` (`id` TEXT NOT NULL, `partyType` TEXT, `code` TEXT, " +
                "`name` TEXT NOT NULL, `phone` TEXT, `email` TEXT, `addressLine1` TEXT, `isActive` INTEGER NOT NULL, " +
                "`updatedAt` TEXT, `searchText` TEXT NOT NULL, PRIMARY KEY(`id`))",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_customers_name` ON `cached_customers` (`name`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_customers_searchText` ON `cached_customers` (`searchText`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_categories` (`id` TEXT NOT NULL, `code` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, `parentId` TEXT, `uomCategoryId` TEXT, `updatedAt` TEXT, PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_uoms` (`id` TEXT NOT NULL, `code` TEXT, `name` TEXT NOT NULL, " +
                "`categoryId` TEXT, `uomType` TEXT, `factor` TEXT NOT NULL, `rounding` TEXT NOT NULL, " +
                "`isActive` INTEGER NOT NULL, `updatedAt` TEXT, PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `sync_state` (`entity` TEXT NOT NULL, `cursor` TEXT, " +
                "`lastSyncAt` INTEGER NOT NULL, PRIMARY KEY(`entity`))",
        )
    }
}

/**
 * v3 → v4: add the chat persistence cache (conversations + messages). Purely
 * additive — existing catalog/outbox tables are untouched.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_conversations` (`conversationId` TEXT NOT NULL, " +
                "`channelId` TEXT NOT NULL, `title` TEXT, `status` TEXT NOT NULL, " +
                "`lastMessageText` TEXT, `lastMessageDirection` TEXT, `lastMessageAt` TEXT, " +
                "`unreadCount` INTEGER NOT NULL, `unreadDelta` INTEGER NOT NULL, " +
                "`cachedAt` INTEGER NOT NULL, PRIMARY KEY(`conversationId`))",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_cached_conversations_lastMessageAt` " +
                "ON `cached_conversations` (`lastMessageAt`)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_chat_messages` (`id` TEXT NOT NULL, " +
                "`conversationId` TEXT NOT NULL, `senderId` TEXT, `receiverId` TEXT, " +
                "`direction` TEXT NOT NULL, `text` TEXT NOT NULL, `questionId` TEXT, " +
                "`attribute` TEXT, `createdAt` TEXT NOT NULL, `status` TEXT, " +
                "PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_cached_chat_messages_conversationId` " +
                "ON `cached_chat_messages` (`conversationId`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_cached_chat_messages_createdAt` " +
                "ON `cached_chat_messages` (`createdAt`)",
        )
    }
}
