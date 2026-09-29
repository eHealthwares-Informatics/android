# Fix log — 2026-09-29 (all implemented, built 13:52, installed on SM-A055F)

## Issue 1: UOM dropdown empty on New Item — FIXED
- Root cause 1 (parse): API `items/dependencies/uoms` returns `rounding: 0.01` (number). App `UomDto.rounding` was `Int?` → Moshi `JsonDataException` → whole list parse failed → `Result.failure` → empty list, error swallowed in `ItemFormViewModel.load()`.
  - Fix: `UomDto.rounding` is now `BigDecimal?` (data/remote/dto/SaleDtos.kt).
- Root cause 2 (pagination): backend caps these endpoints at `limit<=100` (default 20). Categories total 36, UOMs total 115 on production — app fetched only the first 20.
  - Fix: `ItemsApi` gained paginated variants (`limit`, `page`, `search`); `PosRepository.getCategories(search)/getUoms(search)` walk all pages with `limit=100`.
- Resilience: `ItemFormViewModel` now injects `CategoryDao`/`UomDao` and falls back to the Room caches (`cached_categories`, `cached_uoms`, populated by startup sync) when the API fails.
- UX: `ItemFormScreen` picker dialogs always open (previously gated on `state.categories.isNotEmpty()`), include an `AppSearchBar` (debounced server search), loading/empty/error+Retry states. Category field shows live suggestions while typing (typing no longer clobbers the selected id — `categoryQuery` is separate from `categoryId`). UOM field is read-only with a dropdown.

## Issue 2: Receipt crash after POS sale — FIXED
- Crash: `ActivityNotFoundException: No Activity found to handle null` at `ReceiptPrinter.kt:41` (`PrintManager.print`) — device has no print service enabled.
- Fix: `printReceipt` guards the print call; on failure it renders the receipt to a `PdfDocument` in `cacheDir/receipts/` and opens the share sheet (ACTION_SEND, FileProvider authority `${applicationId}.fileprovider`). Manifest gained the FileProvider + `res/xml/file_paths.xml` (cache-path `receipts/`).
- `PosTerminalScreen` checkout effect now: try/catch around receipt, uses server `SaleDto` (real lines/paid/change), and calls `viewModel.resetCheckoutState()` so the LaunchedEffect cannot refire.

## Issue 3: New Order — combine 3 fields into 1 ("IT, GD") — DONE
- `CreateOrderScreen` state replaced: single `lineQuery` + `selItemId`/`selItemLabel` + `selGenericCode`/`selGenericName`.
- `CreateOrderFields.kt` rewritten as `CreateOrderLineFields`: one `AppTextField` labelled "IT, GD" (placeholder "Item / generic, or free text"), merged catalog+generic suggestions (max 6, with `code · item` / `code · generic` subtitles, "Added" state for dupes), Quantity/Unit price in one row, Add line enabled when a pick exists or text is non-blank.
- Free-text rule: if nothing was picked, the typed text becomes `freetextName` on the line.
- Wire format unchanged (`CreateOrderItem(itemId, freetextName, genericItemCode, ...)`), so the backend/order payload is identical.

---

(Original plan below, kept for reference)
# RxSoft Mobile App — Improvement Plan

_Created: 2026-09-27 | Status: PLANNING — no changes yet_

---

## Issue 1: Product Creation — Missing Required Backend Parameters

**Problem:** When creating a product on mobile, the form collects Code, Name, Category, Barcode, and Active toggle. However the backend `CreateItemDto` (swagger) requires three fields: `name`, `categoryId`, and `baseUomId`. The mobile form does **not** collect `baseUomId` (UOM), so the API call will fail with a validation error on the backend.

Additionally, the mobile `CreateItemRequest` sends `code` as required, but the backend does not require it — the backend auto-generates codes. The mobile form should not force code entry.

**Current state:**
- `ItemFormScreen.kt` — fields: Code, Name, Category, Barcode, Active, Image
- `ItemFormViewModel.kt` — builds `CreateItemRequest(code, name, categoryId, barcode, isActive, imageUrl)`
- `CreateItemRequest` DTO has `baseUomId: String? = null` (optional in mobile, **required** by backend)
- Backend `CreateItemDto` required fields: `name`, `categoryId`, `baseUomId`

**Fix:**
1. Add a UOM selector to `ItemFormScreen` — dropdown/search that lists UOMs from `cached_uoms` (already synced)
2. Add `baseUomId` to `ItemFormState` and wire up the selector in the ViewModel
3. Make `code` optional in the form (backend auto-generates if blank)
4. Update save validation: require `name`, `categoryId`, and `baseUomId` (remove `code` requirement for new items)
5. Pass `baseUomId` into `CreateItemRequest`

**Files to change:**
- `ui/items/ItemFormScreen.kt` — add UOM selector field
- `ui/items/ItemFormViewModel.kt` — add UOM state, load UOMs, update validation, pass to request
- `data/remote/dto/ItemDtos.kt` — no change needed (baseUomId already optional in DTO)

---

## Issue 2: Sync Timeout — "Continue Offline" Should Not Stop Sync

**Problem:** When the welcome screen sync times out and "Continue offline" is clicked, the sync should not stop unless the error is a backend HTTP error. Currently, `continueOffline()` cancels the backoff job and moves to Main. The `OfflineSyncManager` continues independently, but there's a subtlety: the initial sync via `beginStartupSync()` is cancelled, and the background manager only re-runs on network state changes — not on a timer.

**Current state:**
- `AuthViewModel.continueOffline()` — cancels `backoffJob`, sets screen to Main
- `OfflineSyncManager.start()` — registers a `NetworkCallback`; `refreshAndSync()` runs on `onAvailable`
- If the device is already online but the sync timed out (slow server), `OfflineSyncManager` won't re-trigger until the network briefly drops and reappears

**Fix:**
1. In `AuthViewModel.continueOffline()`: after setting Main screen, schedule a background retry via `OfflineSyncManager.refreshAndSync()` with a short delay (e.g. 30s), so the sync continues even without a network change
2. In `SyncRepository.ensureSynced()`: distinguish between HTTP errors (5xx/4xx) and timeout/cancellation errors. HTTP 5xx should retry; cancellation (user-initiated) should stop
3. When "Continue offline" is clicked: only cancel if the error was a non-retryable HTTP error (4xx). For timeouts and network errors, continue retrying in background

**Files to change:**
- `ui/auth/AuthViewModel.kt` — modify `continueOffline()` to schedule background retry
- `data/repository/SyncRepository.kt` — improve error classification in `ensureSynced()`
- Possibly `util/OfflineSyncManager.kt` — add a delayed retry method

---

## Issue 3: Medical Art Backdrop on Every Screen

**Problem:** `MedicalArtBackdrop` is currently only used on `LoginScreen`. The user wants it on every screen of the app.

**Current state:**
- `MedicalArtBackdrop.kt` — a composable that renders a decorative medical-themed vector drawable
- Only imported/used in `LoginScreen.kt`
- Each screen is a standalone `@Composable` with its own `Scaffold`

**Fix:**
Two approaches (recommend **Option A**):

**Option A — Centralized in AppNavigation (recommended):**
1. Wrap the `NavHost` content in `MainContent()` and `ShopperScaffold()` with a `Box` that places `MedicalArtBackdrop()` as the first child behind all content
2. This ensures every routed screen gets the backdrop automatically
3. No changes to individual screens needed

**Option B — Per-screen (not recommended):**
Add `MedicalArtBackdrop()` to every individual screen composable. More code changes, harder to maintain.

**Files to change:**
- `ui/navigation/AppNavigation.kt` — wrap `NavHost` in `Box { MedicalArtBackdrop(); NavHost(...) }` in both `MainContent()` and `ShopperScaffold()`
- `ui/splash/SplashScreen.kt` — optionally add backdrop here too
- `ui/sync/SyncLoadingScreen.kt` — optionally add backdrop here too

---

## Issue 4: Offline POS — Payment Modes, Stock Adjustments, and Sales

**Problem:** When there is no internet, the cash modal prevents completing a sale because `loadPaymentMethods()` fetches from the API and fails. Payment modes, stock adjustments, and sales should work offline and sync when internet is detected.

**Current state:**
- `PosTerminalViewModel.loadPaymentMethods()` — calls `posRepository.getPaymentMethods()` which hits the API; on failure sets `UiState.Error`
- Payment methods are NOT cached in the local database
- Stock adjustments (`InventoryRepository.adjustStock()`) — hits the API directly, no offline fallback
- Sales (`PosRepository.submitSale()`) — **already has offline fallback** via `pending_sales` outbox table ✅
- `OfflineSyncManager` — already pushes pending sales on reconnect ✅

**What's already working:**
- ✅ Sales offline queuing (pending_sales table + syncPendingSales)
- ✅ OfflineSyncManager pushes queued sales on reconnect

**What needs fixing:**
1. **Cache payment methods locally** — add a `cached_payment_methods` table to Room DB, sync during startup, read from cache when offline
2. **Stock adjustments offline** — add a `pending_stock_adjustments` outbox table, queue adjustments when offline, push on reconnect
3. **Payment method selection offline** — when offline, load from local cache instead of API

**Detailed fix:**

### 4a. Cache Payment Methods
1. Add `CachedPaymentMethodEntity` to `OfflineEntities.kt`:
   ```kotlin
   @Entity(tableName = "cached_payment_methods")
   data class CachedPaymentMethodEntity(
       @PrimaryKey val id: String,
       val code: String?,
       val name: String,
       val methodType: String,
       val isActive: Boolean,
   )
   ```
2. Add `PaymentMethodDao` to `OfflineDatabase.kt`
3. Sync payment methods during startup sync (add to `SyncRepository`)
4. In `PosRepository.getPaymentMethods()` — try API first, fall back to local cache
5. In `PosTerminalViewModel.loadPaymentMethods()` — load from local cache when offline

### 4b. Offline Stock Adjustments
1. Add `PendingStockAdjustmentEntity` to `OfflineEntities.kt`:
   ```kotlin
   @Entity(tableName = "pending_stock_adjustments")
   data class PendingStockAdjustmentEntity(
       @PrimaryKey(autoGenerate = true) val id: Long = 0,
       val clientRef: String,
       val adjustmentJson: String,
       val createdAt: Long,
       val pushAttempts: Int = 0,
       val lastError: String? = null,
   )
   ```
2. Add `PendingStockAdjustmentDao` to `OfflineDatabase.kt`
3. Bump database version (5 → 6) with migration
4. In `InventoryRepository.adjustStock()` — try API first, fall back to queuing in outbox
5. In `OfflineSyncManager.refreshAndSync()` — push pending stock adjustments on reconnect

### 4c. Update OfflineSyncManager
1. Add `syncPendingStockAdjustments()` to `InventoryRepository`
2. Call it from `OfflineSyncManager.refreshAndSync()` after sales/orders sync

**Files to change:**
- `data/local/OfflineEntities.kt` — add CachedPaymentMethodEntity, PendingStockAdjustmentEntity
- `data/local/OfflineDatabase.kt` — add DAOs, bump version, add migration
- `data/repository/PosRepository.kt` — add payment method caching, offline fallback
- `data/repository/InventoryRepository.kt` — add offline stock adjustment queuing + sync
- `ui/pos/PosTerminalViewModel.kt` — load payment methods from cache when offline
- `util/OfflineSyncManager.kt` — add stock adjustment sync
- `data/remote/api/*.kt` — possibly add payment methods sync API endpoint (check if exists)

---

## Execution Order

1. **Issue 1** (Product creation UOM) — standalone, no dependencies
2. **Issue 3** (Backdrop on all screens) — standalone, quick win
3. **Issue 2** (Sync timeout behavior) — standalone, medium complexity
4. **Issue 4** (Offline POS) — largest scope, depends on understanding the full offline architecture

Issues 1 and 3 can be done in parallel. Issue 2 is independent. Issue 4 is the biggest lift.

---

## Open Questions

1. **UOM selector** — should it be a dropdown (from cached UOMs) or a text field? Dropdown is better UX since UOMs are synced locally.
2. **Payment method caching** — should we sync payment methods during the startup sync, or on-demand? Startup sync is cleaner.
3. **Stock adjustment conflict resolution** — when pushing a queued adjustment, what if the stock balance has changed server-side? Need to decide: last-write-wins, or require user confirmation?
4. **Code field** — should we keep the code field in the product form (optional) or remove it entirely? Backend auto-generates, so optional seems right.
