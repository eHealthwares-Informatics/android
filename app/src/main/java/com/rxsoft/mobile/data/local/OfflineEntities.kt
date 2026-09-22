package com.rxsoft.mobile.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached org catalog item for offline search, order creation and POS selling.
 * Mirrors the organisation-item + item payload from the sync API.
 */
@Entity(
    tableName = "cached_items",
    indices = [
        Index("name"),
        Index("code"),
        Index("barcode"),
        Index("searchText"),
    ],
)
data class CachedItemEntity(
    @PrimaryKey val itemId: String,
    val orgItemId: String? = null,
    val name: String,
    val displayName: String?,
    val code: String?,
    val barcode: String?,
    val categoryId: String? = null,
    val categoryCode: String? = null,
    val categoryName: String? = null,
    val baseUomId: String?,
    val baseUomCode: String? = null,
    val baseUomName: String? = null,
    val saleUomId: String? = null,
    val saleUomCode: String? = null,
    val saleUomName: String? = null,
    val genericProductCode: String? = null,
    val genericProductName: String? = null,
    val imageUrl: String?,
    val smallImageUrl: String? = null,
    val updatedAt: String? = null,
    val searchText: String = "",
    val cachedAt: Long,
)

/** Price list header (all lists for the organisation). */
@Entity(tableName = "cached_price_lists")
data class CachedPriceListEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
    val updatedAt: String? = null,
)

/** A single price list item (unit price for an item within a price list). */
@Entity(
    tableName = "cached_prices",
    primaryKeys = ["priceListId", "itemId"],
    indices = [Index("itemId"), Index("priceListId")],
)
data class CachedPriceEntity(
    val priceListId: String,
    val itemId: String,
    val unitPrice: String,
    val currencyCode: String? = null,
    val startsAt: String? = null,
    val endsAt: String? = null,
    val updatedAt: String? = null,
)

/** Stock location for the organisation. */
@Entity(tableName = "cached_stock_locations")
data class CachedStockLocationEntity(
    @PrimaryKey val id: String,
    val code: String? = null,
    val name: String,
    val locationType: String? = null,
    val isActive: Boolean = true,
    val parentId: String? = null,
    val updatedAt: String? = null,
)

/** Stock balance for an item at a location (optionally per lot). */
@Entity(
    tableName = "cached_stock_balances",
    indices = [Index("itemId"), Index("locationId")],
)
data class CachedStockBalanceEntity(
    @PrimaryKey val id: String,
    val itemId: String? = null,
    val itemCode: String? = null,
    val itemName: String? = null,
    val locationId: String? = null,
    val locationName: String? = null,
    val lotId: String? = null,
    val lotCode: String? = null,
    val quantityOnHand: String,
    val quantityReserved: String,
    val averageCost: String? = null,
    val reorderMinQty: String? = null,
    val reorderMaxQty: String? = null,
    val updatedAt: String? = null,
)

/** Customer party for offline lookup. */
@Entity(
    tableName = "cached_customers",
    indices = [Index("name"), Index("searchText")],
)
data class CachedCustomerEntity(
    @PrimaryKey val id: String,
    val partyType: String? = null,
    val code: String? = null,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val addressLine1: String? = null,
    val isActive: Boolean = true,
    val updatedAt: String? = null,
    val searchText: String = "",
)

/** Item category (global reference data). */
@Entity(tableName = "cached_categories")
data class CachedCategoryEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val parentId: String? = null,
    val uomCategoryId: String? = null,
    val updatedAt: String? = null,
)

/** Unit of measure (global reference data). */
@Entity(tableName = "cached_uoms")
data class CachedUomEntity(
    @PrimaryKey val id: String,
    val code: String? = null,
    val name: String,
    val categoryId: String? = null,
    val uomType: String? = null,
    val factor: String = "1",
    val rounding: String = "0.01",
    val isActive: Boolean = true,
    val updatedAt: String? = null,
)

/** Per-entity sync cursor + last sync time. */
@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val entity: String,
    val cursor: String? = null,
    val lastSyncAt: Long,
)

/** An item plus its unit price for a given price list (query projection). */
data class CatalogItemWithPrice(
    @Embedded val item: CachedItemEntity,
    val unitPrice: String? = null,
)

/**
 * Cached conversation inbox row for the chat feature. Persists across app
 * restarts so the inbox renders instantly from Room before the network
 * refresh returns. `unreadCount` mirrors the server value at last sync;
 * `unreadDelta` tracks locally-observed unread messages since then.
 */
@Entity(
    tableName = "cached_conversations",
    indices = [Index("lastMessageAt")],
)
data class CachedConversationEntity(
    @PrimaryKey val conversationId: String,
    val channelId: String,
    val title: String? = null,
    val status: String,
    val lastMessageText: String? = null,
    val lastMessageDirection: String? = null,
    val lastMessageAt: String? = null,
    val unreadCount: Int = 0,
    val unreadDelta: Int = 0,
    val cachedAt: Long,
)

/** Cached chat message for a conversation (last page per conversation). */
@Entity(
    tableName = "cached_chat_messages",
    indices = [Index("conversationId"), Index("createdAt")],
)
data class CachedChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String? = null,
    val receiverId: String? = null,
    val direction: String,
    val text: String,
    val questionId: String? = null,
    val attribute: String? = null,
    val createdAt: String,
    val status: String? = null,
)

/**
 * An order created while offline (or whose push failed). Held in an outbox
 * table and pushed to the server when connectivity returns.
 */
@Entity(tableName = "pending_orders")
data class PendingOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Client-generated UUID used as idempotency key when pushing. */
    val clientRef: String,
    val paymentMethod: String,
    /** JSON-serialized list of CreateOrderItem. */
    val itemsJson: String,
    val createdAt: Long,
    val pushAttempts: Int = 0,
    val lastError: String? = null,
)
