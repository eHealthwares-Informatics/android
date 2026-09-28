# Graph Report - rxsoft-mobile  (2026-09-28)

## Corpus Check
- 223 files · ~85,358 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2022 nodes · 3427 edges · 182 communities (143 shown, 39 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 283 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `7861dd86`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Inventory Items API
- UI State Components
- Cart & Checkout UI
- Stock Adjustment
- Checkout ViewModel
- Medicine Catalog
- Sales Reports API
- Item List Screen
- POS Sale Creation
- PIN Entry Screen
- Customer API
- App Navigation
- Authentication API
- Auth ViewModel
- PIN Manager
- Item Form ViewModel
- Receipt Printing
- Network & Payment Methods
- Login Screen
- Text Field & Item Form
- Common API DTOs
- Product Detail UI
- Design System Roles
- Medicine & Profile UI
- HTTP Logging Interceptor
- Token Manager Module
- POS Terminal Search
- Settings ViewModel
- Configuration API
- Bottom Nav & Stock Adjustment
- Prescription Upload
- Snackbar Component
- Session Manager
- BigDecimal Adapter
- Top App Bar
- Prescription Upload Cards
- Pricing API
- Profile Screen
- Module Config
- Settings Screen
- Server URL Manager
- Image Upload API
- Auth Interceptor
- Chip Component
- Dialog Components
- Badge Component
- Gradle Wrapper Script
- Application Class
- Mobile Theme
- Color Tokens
- Cart Item Model
- Theme Definition
- Repository Module
- Elevation Tokens
- Motion Tokens
- Shape Tokens
- Spacing Tokens
- App Module
- Constants
- Terminal Script
- Type Definitions
- Launcher Icon HDPI
- Round Launcher Icon
- Layout Pattern Engineer
- Screen Generator
- Compose Performance Reviewer
- Settings Screen Engineer
- Data Table Engineer
- Dashboard Engineer
- Healthcare UX Specialist
- Preview Generator
- Principal Design Engineer
- 09-material3-expert.md
- RxSoft Mobile Agent
- ListScreenTemplate
- ListResponse
- ReportsApi
- LoginScreen
- host.sh
- 10-android-animation-expert.md
- 11-compose-testing-expert.md
- SalesApi
- Compose Screen — rxsoft-mobile
- 18-form-engineer.md
- AppEmptyState
- 21-healthcare-ux.md
- 22-state-management.md
- 24-theme-customization.md
- ProductDetailScreen
- AppCard
- 13-design-system-documentation.md
- 19-data-table-engineer.md
- 20-dashboard-engineer.md
- AppErrorState
- 23-preview-generator.md
- dependencies
- Design Token Engineer
- Component Engineer
- Accessibility Specialist
- Material 3 Expert
- Android Animation Expert
- Compose Testing Expert
- Design System Documentation Expert
- Theme Engineer
- Form Engineer
- State Management Engineer
- Theme Customization Engineer
- PriceListViewModel
- RxSoft Mobile App — Improvement Plan
- OfflineItemDao
- PendingStockAdjustmentDao
- StockLocationDao
- ListResponse
- PaymentMethodDto
- AppTopAppBar
- SimpleBarChart
- MainContent
- CheckoutScreen
- ThemeViewModel
- OfflineDatabase.kt
- PriceListDao
- StockBalanceDao
- UomDao
- .items
- AppErrorState
- ThemePreferences
- ItemListViewModel
- CreateOrderCascadeFields
- OfflineSyncManager
- ConversationListScreen
- .provideOkHttpClient
- MedicineCatalogScreen
- PosOrderListViewModel
- CartItemCard
- ServerUrlManager
- SessionManager
- OrderDtos.kt
- ChatScreen
- AppearanceMode
- OrderLinesScreen
- UploadPrescriptionScreen
- BigDecimalAdapter
- ServerUrlInterceptor
- .parse
- AppBottomNav
- RxSoftTheme
- PosOrderDetailViewModel
- PosOrderListScreen
- PriceListItemsScreen
- ModuleConfig
- PaymentSummary
- SyncSettingsManager
- make_launcher_icons.py
- AppAlertDialog
- DateRangeFilterRow
- RoundedIconButton
- AppBadge
- ColorTokens.kt
- LazyLinesAccordion
- CartItem
- RepositoryModule.kt
- ElevationTokens.kt
- MotionTokens.kt
- ShapeTokens.kt
- SpacingTokens.kt
- AppModule.kt
- Constants.kt

## God Nodes (most connected - your core abstractions)
1. `PosTerminalViewModel` - 38 edges
2. `MainContent()` - 35 edges
3. `UiState` - 34 edges
4. `OfflineDatabase` - 33 edges
5. `AuthViewModel` - 33 edges
6. `AppCard()` - 32 edges
7. `Screen` - 31 edges
8. `PurchasesViewModel` - 27 edges
9. `CheckoutViewModel` - 26 edges
10. `ChatRepository` - 25 edges

## Surprising Connections (you probably didn't know these)
- `PosTerminalScreen()` --calls--> `ReceiptLine`  [INFERRED]
  app/src/main/java/com/ehealthwares/rxsoft/ui/pos/PosTerminalScreen.kt → app/src/main/java/com/ehealthwares/rxsoft/util/ReceiptPrinter.kt
- `ProductDetailScreen()` --references--> `Product`  [EXTRACTED]
  snippets/ProductDetailScreen.kt → app/src/main/java/com/ehealthwares/rxsoft/ui/shop/model/Product.kt
- `toItemDto()` --references--> `ItemDto`  [EXTRACTED]
  app/src/main/java/com/ehealthwares/rxsoft/data/remote/dto/ItemDtos.kt → app/src/main/java/com/ehealthwares/rxsoft/data/remote/dto/SaleDtos.kt
- `CustomerSearchDialog()` --calls--> `PartyDto`  [INFERRED]
  app/src/main/java/com/ehealthwares/rxsoft/ui/pos/PosTerminalScreen.kt → app/src/main/java/com/ehealthwares/rxsoft/data/remote/dto/SaleDtos.kt
- `toItemDto()` --calls--> `ReferenceDto`  [INFERRED]
  app/src/main/java/com/ehealthwares/rxsoft/data/repository/PosRepository.kt → app/src/main/java/com/ehealthwares/rxsoft/data/remote/dto/SaleDtos.kt

## Import Cycles
- None detected.

## Communities (182 total, 39 thin omitted)

### Community 0 - "Inventory Items API"
Cohesion: 0.40
Nodes (3): UploadImageResponse, File, MultipartBody

### Community 1 - "UI State Components"
Cohesion: 0.06
Nodes (50): AppSideDrawer(), DrawerMenuItem, DrawerMenuSection, androidx, List, String, PharmacyMenuSections, Modifier (+42 more)

### Community 2 - "Cart & Checkout UI"
Cohesion: 0.06
Nodes (32): CartItemCard(), CartItem, Modifier, AddProductCard(), CheckoutScreen(), EmptyCartCard(), CartItem, List (+24 more)

### Community 3 - "Stock Adjustment"
Cohesion: 0.07
Nodes (28): Map, String, PurchasesApi, UpdatePurchaseStatusRequest, CreatePurchaseLine, CreatePurchaseRequest, PurchaseDto, PurchaseLineDto (+20 more)

### Community 4 - "Checkout ViewModel"
Cohesion: 0.06
Nodes (29): AuthApi, AuthResponse, CurrentUserResponse, LoginRequest, ModuleInfoDto, RefreshRequest, ShopperOtpResponse, ShopperRequestOtpRequest (+21 more)

### Community 5 - "Medicine Catalog"
Cohesion: 0.08
Nodes (24): Map, String, PricingApi, OrganisationConfig, PriceListDto, PriceListItemDto, AdjustItemPriceRequest, CreatePriceListItemRequest (+16 more)

### Community 6 - "Sales Reports API"
Cohesion: 0.07
Nodes (26): AppScreen, AuthViewModel, Boolean, Job, SharedFlow, StateFlow, String, Unit (+18 more)

### Community 7 - "Item List Screen"
Cohesion: 0.46
Nodes (7): Error, Idle, Loading, Success, UiState, Nothing, T

### Community 8 - "POS Sale Creation"
Cohesion: 0.07
Nodes (25): AdjustStockRequest, LotDto, StockBalanceDto, StockLocationDto, InventoryRepository, Int, List, Result (+17 more)

### Community 9 - "PIN Entry Screen"
Cohesion: 0.07
Nodes (21): Double, Int, Job, List, StateFlow, String, ViewModel, MedicineCatalogViewModel (+13 more)

### Community 10 - "Customer API"
Cohesion: 0.09
Nodes (21): Entities, Boolean, Int, List, Map, StateFlow, String, PageResult (+13 more)

### Community 11 - "App Navigation"
Cohesion: 0.25
Nodes (5): Bundle, MainActivity, Boolean, RxSoftMobileTheme(), ComponentActivity

### Community 12 - "Authentication API"
Cohesion: 0.09
Nodes (24): Map, String, SyncApi, SyncCategoriesResponse, SyncCategoryDto, SyncCustomerDto, SyncCustomersResponse, SyncDeletedRef (+16 more)

### Community 13 - "Auth ViewModel"
Cohesion: 0.11
Nodes (22): DeleteButton(), EnterPinScreen(), KeyButton(), Boolean, Int, String, PinBottomActions(), PinDots() (+14 more)

### Community 14 - "PIN Manager"
Cohesion: 0.11
Nodes (17): android, AnalyticsViewModel, Boolean, Context, Int, LocalDate, StateFlow, String (+9 more)

### Community 15 - "Item Form ViewModel"
Cohesion: 0.13
Nodes (7): ItemFormState, ItemFormViewModel, Boolean, StateFlow, String, ViewModel, Uri

### Community 16 - "Receipt Printing"
Cohesion: 0.14
Nodes (14): Bundle, Context, printReceipt(), ReceiptData, ReceiptLine, ReceiptPrintAdapter, Array, CancellationSignal (+6 more)

### Community 17 - "Network & Payment Methods"
Cohesion: 0.12
Nodes (7): ConfigApi, GenericProductsApi, PaymentMethodsApi, UploadApi, Moshi, NetworkModule, Retrofit

### Community 18 - "Login Screen"
Cohesion: 0.11
Nodes (14): CartItem, Idle, BigDecimal, Boolean, Job, List, StateFlow, String (+6 more)

### Community 19 - "Text Field & Item Form"
Cohesion: 0.14
Nodes (12): AppTextField(), Boolean, ImageVector, Modifier, String, Unit, ItemFormScreen(), String (+4 more)

### Community 20 - "Common API DTOs"
Cohesion: 0.18
Nodes (10): Annotation, ApiErrorDetail, ApiErrorResponse, Moshi, Set, ListResponseAdapterFactory, ListResponseMeta, PaginationQuery (+2 more)

### Community 21 - "Product Detail UI"
Cohesion: 0.12
Nodes (15): Always Check, Buttons, Contrast, Focus, Forms, Healthcare Rules, Images, Lists (+7 more)

### Community 23 - "Medicine & Profile UI"
Cohesion: 0.29
Nodes (10): Medicine, MedicineCard(), MedicineCatalogScreen(), sampleMedicines(), BottomNavigationBar(), androidx, Int, String (+2 more)

### Community 24 - "HTTP Logging Interceptor"
Cohesion: 0.10
Nodes (16): CustomersApi, Map, String, CreateCustomerRequest, CustomerDto, CustomerRepository, Int, List (+8 more)

### Community 25 - "Token Manager Module"
Cohesion: 0.24
Nodes (8): bodyToString(), Interceptor, Map, Response, String, redactHeaders(), TraceLoggingInterceptor, okhttp3

### Community 26 - "POS Terminal Search"
Cohesion: 0.14
Nodes (8): CachedChatMessageDao, CachedConversationDao, Flow, List, String, CachedChatMessageEntity, CachedConversationEntity, CatalogItemWithPrice

### Community 27 - "Settings ViewModel"
Cohesion: 0.10
Nodes (12): ChatStateStore, Keys, Flow, Int, String, LastOpen, AppModule, Context (+4 more)

### Community 28 - "Configuration API"
Cohesion: 0.20
Nodes (24): AnalyticsScreen(), CategoryRow(), formatNaira(), androidx, Double, Int, String, Unit (+16 more)

### Community 29 - "Bottom Nav & Stock Adjustment"
Cohesion: 0.11
Nodes (14): Map, String, SalesApi, CategoryDto, CreateSaleLine, CreateSalePayment, CreateSaleRequest, PartyDto (+6 more)

### Community 30 - "Prescription Upload"
Cohesion: 0.36
Nodes (8): BottomFloatingBar(), BrowseCard(), CreateRequestCard(), Int, Modifier, String, UploadCard(), UploadPrescriptionScreen()

### Community 31 - "Snackbar Component"
Cohesion: 0.16
Nodes (12): AppSnackbarHost(), Modifier, String, Unit, showInfo(), String, Unit, PriceListCard() (+4 more)

### Community 32 - "Session Manager"
Cohesion: 0.16
Nodes (9): ChatRepository, ConversationEnded, Boolean, List, Map, SharedFlow, StateFlow, String (+1 more)

### Community 33 - "BigDecimal Adapter"
Cohesion: 0.11
Nodes (7): androidx, OfflineDatabase, PaymentMethodDao, PriceDao, CachedPaymentMethodEntity, Context, OfflineModule

### Community 34 - "Top App Bar"
Cohesion: 0.23
Nodes (7): ItemDto, BigDecimal, Int, List, Result, String, PosRepository

### Community 35 - "Prescription Upload Cards"
Cohesion: 0.14
Nodes (11): CheckoutViewModel, Double, Int, Intent, List, SharedFlow, StateFlow, String (+3 more)

### Community 36 - "Pricing API"
Cohesion: 0.13
Nodes (12): List, String, PaymentsApi, AvailablePaymentProviderDto, InitializePaymentRequest, InitializePaymentResponse, PaymentProviderRef, VerifyPaymentResponse (+4 more)

### Community 37 - "Profile Screen"
Cohesion: 0.19
Nodes (18): AppIconButton(), AppOutlinedButton(), AppPrimaryButton(), AppSecondaryButton(), AppTextButton(), Boolean, ImageVector, Modifier (+10 more)

### Community 38 - "Module Config"
Cohesion: 0.15
Nodes (13): CreateOrderItem, buildItem(), Catalog, Generic, BigDecimal, Boolean, Int, List (+5 more)

### Community 39 - "Settings Screen"
Cohesion: 0.14
Nodes (9): BigDecimal, Int, Job, List, StateFlow, String, Unit, ViewModel (+1 more)

### Community 40 - "Server URL Manager"
Cohesion: 0.20
Nodes (6): Boolean, Int, SharedPreferences, String, PinManager, StoredCredentials

### Community 41 - "Image Upload API"
Cohesion: 0.15
Nodes (12): API Services, Architecture, Build Variants, Design System, Features, Layers, Navigation, Project Structure (+4 more)

### Community 42 - "Auth Interceptor"
Cohesion: 0.12
Nodes (5): Long, PendingOrderDao, PendingSaleDao, PendingOrderEntity, PendingSaleEntity

### Community 43 - "Chip Component"
Cohesion: 0.21
Nodes (9): GenericProductDto, Int, List, Result, String, OrdersRepository, OrderSubmitResult, Pushed (+1 more)

### Community 44 - "Dialog Components"
Cohesion: 0.17
Nodes (10): ChatApi, Boolean, Int, Response, String, Unit, ConversationInboxResponse, ExchangeMessagesResponse (+2 more)

### Community 45 - "Badge Component"
Cohesion: 0.16
Nodes (13): DailySalesRow, DailyRowCard(), DailySalesScreen(), List, Unit, ReportSummaryCard(), TopItemCard(), DailySalesViewModel (+5 more)

### Community 46 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 47 - "Application Class"
Cohesion: 0.33
Nodes (4): RxSoftApplication, Application, ImageLoader, ImageLoaderFactory

### Community 48 - "Mobile Theme"
Cohesion: 0.18
Nodes (14): SaleLineDto, formatSaleLineDate(), String, Unit, SaleLineCard(), saleLineRowKey(), SaleLinesScreen(), List (+6 more)

### Community 49 - "Color Tokens"
Cohesion: 0.15
Nodes (8): ItemsApi, List, Map, String, CreateItemRequest, OrgItemDto, PatchItemRequest, toItemDto()

### Community 50 - "Cart Item Model"
Cohesion: 0.20
Nodes (10): ExchangeMessage, ChatViewModel, ConversationListViewModel, Boolean, List, Map, SharedFlow, StateFlow (+2 more)

### Community 51 - "Theme Definition"
Cohesion: 0.17
Nodes (10): List, String, WebsiteApi, OrderDto, Unit, OrderCard(), OrderDetailDialog(), OrderLineRow() (+2 more)

### Community 52 - "Repository Module"
Cohesion: 0.34
Nodes (14): AppEmptyStateDashboard(), AppErrorStateDashboard(), AppLoadingStateDashboard(), AppointmentRow(), DashboardKpiCard(), DashboardQuickAction(), Color, ImageVector (+6 more)

### Community 53 - "Elevation Tokens"
Cohesion: 0.14
Nodes (13): PaymentMethodSummary, PurchasesAnalyticsSummary, PurchasesByCategory, PurchasesByLocation, PurchasesByStatus, PurchasesBySupplier, PurchasesRecent, PurchasesTopSupplier (+5 more)

### Community 54 - "Motion Tokens"
Cohesion: 0.23
Nodes (8): PurchasesAnalytics, SalesAnalytics, TopSellingItem, List, ResponseBody, Result, String, ReportsRepository

### Community 55 - "Shape Tokens"
Cohesion: 0.22
Nodes (13): ColorThemeSection(), CustomColorSection(), Boolean, Color, Long, Modifier, String, Unit (+5 more)

### Community 56 - "Spacing Tokens"
Cohesion: 0.27
Nodes (13): CartItemRow(), CustomerSearchDialog(), androidx, Boolean, CartItem, List, String, NoStockBalanceDialog() (+5 more)

### Community 57 - "App Module"
Cohesion: 0.18
Nodes (3): CustomerDao, Int, CachedCustomerEntity

### Community 58 - "Constants"
Cohesion: 0.22
Nodes (6): Boolean, Boolean, Intent, String, PaymentAppDetector, PaymentProviderType

### Community 61 - "Type Definitions"
Cohesion: 0.18
Nodes (3): CategoryDao, CachedCategoryEntity, CachedPriceEntity

### Community 76 - "09-material3-expert.md"
Cohesion: 0.18
Nodes (10): Components, Icons, Layout, Mission, Motion, Never, Output, Principles (+2 more)

### Community 77 - "RxSoft Mobile Agent"
Cohesion: 0.20
Nodes (9): Adding a screen, API base URL, Architecture, Auth, Design system, Key commands, Overview, RxSoft Mobile Agent (+1 more)

### Community 78 - "ListScreenTemplate"
Cohesion: 0.20
Nodes (7): CouponsApi, CouponValidationDto, ValidateCouponRequest, CouponsRepository, Double, Result, String

### Community 79 - "ListResponse"
Cohesion: 0.27
Nodes (6): List, Map, Response, ResponseBody, String, ReportsApi

### Community 80 - "ReportsApi"
Cohesion: 0.29
Nodes (9): OrderItemDto, List, StateFlow, String, ViewModel, orderLineKind(), orderLineLabel(), OrderLineRow (+1 more)

### Community 81 - "LoginScreen"
Cohesion: 0.23
Nodes (5): ChatSocket, Boolean, StateFlow, String, Socket

### Community 82 - "host.sh"
Cohesion: 0.53
Nodes (8): build_apk(), die(), log(), running_pid(), host.sh script, status(), stop_server(), urls()

### Community 83 - "10-android-animation-expert.md"
Cohesion: 0.22
Nodes (8): Durations, Healthcare, Mission, Never, Output, Performance, Preferred APIs, Use Cases

### Community 84 - "11-compose-testing-expert.md"
Cohesion: 0.22
Nodes (8): Accessibility, Assertions, Generate, Mission, Output, Performance, Test Tags, Verify

### Community 85 - "SalesApi"
Cohesion: 0.27
Nodes (11): AppLinearProgress(), AppLoadingOverlay(), AppLoadingState(), AppPageLoading(), AppSkeletonLoader(), Boolean, Int, Modifier (+3 more)

### Community 86 - "Compose Screen — rxsoft-mobile"
Cohesion: 0.25
Nodes (7): Compose Screen — rxsoft-mobile, Inputs, Purpose, Refactoring, When not to invoke, When to invoke, Workflow

### Community 87 - "18-form-engineer.md"
Cohesion: 0.25
Nodes (7): Generate, Input Components, Mission, Output, Principles, States, Validation

### Community 88 - "AppEmptyState"
Cohesion: 0.27
Nodes (11): formatSyncTime(), Boolean, ImageVector, List, Long, String, Unit, ModuleToggleCard() (+3 more)

### Community 89 - "21-healthcare-ux.md"
Cohesion: 0.29
Nodes (6): Mission, Never, Optimize, Output, Principles, Users

### Community 90 - "22-state-management.md"
Cohesion: 0.29
Nodes (6): Avoid, Generate, Mission, Output, Principles, Support

### Community 91 - "24-theme-customization.md"
Cohesion: 0.29
Nodes (6): Architecture, Generate, Mission, Output, Persistence, Support

### Community 92 - "ProductDetailScreen"
Cohesion: 0.17
Nodes (9): Boolean, Modifier, String, PrimaryButton(), Modifier, String, ProductImage(), String (+1 more)

### Community 93 - "AppCard"
Cohesion: 0.20
Nodes (9): AppEmptyState(), ImageVector, Modifier, String, Unit, Unit, StockBalanceScreen(), StockCard() (+1 more)

### Community 94 - "13-design-system-documentation.md"
Cohesion: 0.33
Nodes (5): Document, Include, Mission, Output, Style

### Community 95 - "19-data-table-engineer.md"
Cohesion: 0.33
Nodes (5): Features, Healthcare Examples, Mission, Output, Performance

### Community 96 - "20-dashboard-engineer.md"
Cohesion: 0.33
Nodes (5): Charts, Components, Layout, Mission, Output

### Community 97 - "AppErrorState"
Cohesion: 0.45
Nodes (8): ColorTheme, getDarkColors(), getLightColors(), Color, ThemeColors, themePrimaryColor(), themePrimaryColorDark(), ThemeSettings

### Community 98 - "23-preview-generator.md"
Cohesion: 0.40
Nodes (4): Generate, Mission, Output, Preview Data

### Community 120 - "PriceListViewModel"
Cohesion: 0.27
Nodes (6): Boolean, List, StateFlow, String, ViewModel, PriceListViewModel

### Community 121 - "RxSoft Mobile App — Improvement Plan"
Cohesion: 0.18
Nodes (10): 4a. Cache Payment Methods, 4b. Offline Stock Adjustments, 4c. Update OfflineSyncManager, Execution Order, Issue 1: Product Creation — Missing Required Backend Parameters, Issue 2: Sync Timeout — "Continue Offline" Should Not Stop Sync, Issue 3: Medical Art Backdrop on Every Screen, Issue 4: Offline POS — Payment Modes, Stock Adjustments, and Sales (+2 more)

### Community 125 - "ListResponse"
Cohesion: 0.27
Nodes (5): InventoryApi, Map, String, ListResponse, UomDto

### Community 126 - "PaymentMethodDto"
Cohesion: 0.33
Nodes (6): PaymentMethodDto, Pushed, Queued, SaleSubmitResult, toCachedEntity(), toPaymentMethodDto()

### Community 127 - "AppTopAppBar"
Cohesion: 0.24
Nodes (8): AppTopAppBar(), AppTopAppBarActions(), ImageVector, Modifier, String, Unit, Unit, StockAdjustmentScreen()

### Community 128 - "SimpleBarChart"
Cohesion: 0.31
Nodes (9): BarChartData, drawBarChart(), Color, Double, Int, List, Modifier, String (+1 more)

### Community 129 - "MainContent"
Cohesion: 0.24
Nodes (9): Unit, MainContent(), ImageVector, Int, String, Unit, ProfileMenuItem(), ProfileScreen() (+1 more)

### Community 130 - "CheckoutScreen"
Cohesion: 0.27
Nodes (8): AddProductCard(), CheckoutScreen(), EmptyCartCard(), List, PaymentMethodCard(), Modifier, String, VoucherCard()

### Community 131 - "ThemeViewModel"
Cohesion: 0.22
Nodes (5): AndroidViewModel, Long, StateFlow, ThemeSettings, ThemeViewModel

### Community 132 - "OfflineDatabase.kt"
Cohesion: 0.28
Nodes (4): migrate(), SyncStateDao, SyncStateEntity, SupportSQLiteDatabase

### Community 136 - ".items"
Cohesion: 0.28
Nodes (6): CustomerCard(), CustomerListScreen(), Unit, ItemCard(), ItemListScreen(), Unit

### Community 137 - "AppErrorState"
Cohesion: 0.22
Nodes (7): AppErrorState(), Modifier, String, Unit, String, Unit, PosOrderDetailScreen()

### Community 138 - "ThemePreferences"
Cohesion: 0.22
Nodes (5): Keys, Flow, Long, ThemeSettings, ThemePreferences

### Community 139 - "ItemListViewModel"
Cohesion: 0.25
Nodes (6): ItemListViewModel, Boolean, List, StateFlow, String, ViewModel

### Community 140 - "CreateOrderCascadeFields"
Cohesion: 0.25
Nodes (7): CreateOrderCascadeFields(), Boolean, List, String, CreateOrderScreen(), DraftOrderLine, Unit

### Community 141 - "OfflineSyncManager"
Cohesion: 0.28
Nodes (4): Boolean, Long, StateFlow, OfflineSyncManager

### Community 142 - "ConversationListScreen"
Cohesion: 0.32
Nodes (7): ConversationInboxItem, ConversationItem(), ConversationListScreen(), Modifier, String, Unit, toRelativeTime()

### Community 143 - ".provideOkHttpClient"
Cohesion: 0.29
Nodes (4): AuthInterceptor, Interceptor, Response, OkHttpClient

### Community 144 - "MedicineCatalogScreen"
Cohesion: 0.29
Nodes (6): AppSearchBar(), Modifier, String, Unit, MedicineCard(), MedicineCatalogScreen()

### Community 145 - "PosOrderListViewModel"
Cohesion: 0.25
Nodes (5): Boolean, List, StateFlow, ViewModel, PosOrderListViewModel

### Community 146 - "CartItemCard"
Cohesion: 0.25
Nodes (6): CartItemCard(), CartItem, Modifier, Int, Modifier, QuantitySelector()

### Community 147 - "ServerUrlManager"
Cohesion: 0.39
Nodes (3): SharedPreferences, String, ServerUrlManager

### Community 148 - "SessionManager"
Cohesion: 0.32
Nodes (3): Boolean, StateFlow, SessionManager

### Community 149 - "OrderDtos.kt"
Cohesion: 0.29
Nodes (5): Int, String, CreateOrderRequest, GenericProductSearchResponse, OrderItemRefDto

### Community 150 - "ChatScreen"
Cohesion: 0.48
Nodes (6): ChatBubble(), ChatScreen(), String, Unit, OptionCard(), toDisplayTime()

### Community 151 - "AppearanceMode"
Cohesion: 0.29
Nodes (4): DarkModeToggle(), Boolean, Modifier, AppearanceMode

### Community 152 - "OrderLinesScreen"
Cohesion: 0.48
Nodes (6): formatOrderLineDate(), String, Unit, lineRowKey(), OrderLineCard(), OrderLinesScreen()

### Community 153 - "UploadPrescriptionScreen"
Cohesion: 0.43
Nodes (6): BrowseCard(), CreateRequestCard(), Int, String, UploadCard(), UploadPrescriptionScreen()

### Community 154 - "BigDecimalAdapter"
Cohesion: 0.47
Nodes (3): BigDecimalAdapter, BigDecimal, Double

### Community 155 - "ServerUrlInterceptor"
Cohesion: 0.40
Nodes (3): Interceptor, Response, ServerUrlInterceptor

### Community 156 - ".parse"
Cohesion: 0.47
Nodes (4): ChatOptionParser, String, Option, ParsedQuestionOptions

### Community 157 - "AppBottomNav"
Cohesion: 0.40
Nodes (5): AppBottomNav(), BottomNavTab, List, Modifier, String

### Community 158 - "RxSoftTheme"
Cohesion: 0.40
Nodes (5): AppThemeColors, Boolean, ThemeSettings, RxSoftSpacing, RxSoftTheme()

### Community 159 - "PosOrderDetailViewModel"
Cohesion: 0.33
Nodes (4): StateFlow, String, ViewModel, PosOrderDetailViewModel

### Community 160 - "PosOrderListScreen"
Cohesion: 0.40
Nodes (5): Boolean, Unit, PosOrderListScreen(), SaleCard(), scrollingDown

### Community 161 - "PriceListItemsScreen"
Cohesion: 0.53
Nodes (5): EditPriceDialog(), String, Unit, PriceItemRow(), PriceListItemsScreen()

### Community 162 - "ModuleConfig"
Cohesion: 0.47
Nodes (4): AppModule, Set, StateFlow, ModuleConfig

### Community 163 - "PaymentSummary"
Cohesion: 0.47
Nodes (5): Boolean, Modifier, String, PaymentSummary(), SummaryRow()

### Community 164 - "SyncSettingsManager"
Cohesion: 0.40
Nodes (4): Flow, Int, Long, SyncSettingsManager

### Community 165 - "make_launcher_icons.py"
Cohesion: 0.60
Nodes (5): load_logo(), main(), make_background(), make_foreground(), make_legacy()

### Community 166 - "AppAlertDialog"
Cohesion: 0.60
Nodes (4): AppAlertDialog(), AppInfoDialog(), Modifier, String

### Community 167 - "DateRangeFilterRow"
Cohesion: 0.50
Nodes (4): DateRangeFilterRow(), DateRangePreset, List, String

### Community 168 - "RoundedIconButton"
Cohesion: 0.40
Nodes (4): ImageVector, Modifier, String, RoundedIconButton()

### Community 169 - "AppBadge"
Cohesion: 0.50
Nodes (3): AppBadge(), Int, Modifier

### Community 170 - "ColorTokens.kt"
Cohesion: 0.50
Nodes (3): BrandColors, ColorTokens, DamorexColors

### Community 171 - "LazyLinesAccordion"
Cohesion: 0.50
Nodes (3): Boolean, String, LazyLinesAccordion()

## Knowledge Gaps
- **191 isolated node(s):** `@opencode-ai/plugin`, `Keys`, `ModuleInfoDto`, `ListResponseMeta`, `ApiErrorResponse` (+186 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **39 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `UiState` connect `Item List Screen` to `Stock Adjustment`, `Checkout ViewModel`, `Medicine Catalog`, `Sales Reports API`, `POS Sale Creation`, `PIN Entry Screen`, `ItemListViewModel`, `CreateOrderCascadeFields`, `PIN Manager`, `PosOrderListViewModel`, `Login Screen`, `HTTP Logging Interceptor`, `Configuration API`, `PosOrderDetailViewModel`, `Prescription Upload Cards`, `Profile Screen`, `Module Config`, `Settings Screen`, `Badge Component`, `Mobile Theme`, `ReportsApi`, `PriceListViewModel`?**
  _High betweenness centrality (0.176) - this node is a cross-community bridge._
- **Why does `MainContent()` connect `MainContent` to `UI State Components`, `CheckoutScreen`, `Checkout ViewModel`, `Sales Reports API`, `.items`, `AppErrorState`, `Customer API`, `CreateOrderCascadeFields`, `ConversationListScreen`, `MedicineCatalogScreen`, `Text Field & Item Form`, `ChatScreen`, `OrderLinesScreen`, `UploadPrescriptionScreen`, `Configuration API`, `Snackbar Component`, `PosOrderListScreen`, `PriceListItemsScreen`, `Profile Screen`, `Badge Component`, `Mobile Theme`, `Theme Definition`, `Shape Tokens`, `Spacing Tokens`, `AppEmptyState`, `ProductDetailScreen`, `AppCard`, `AppTopAppBar`?**
  _High betweenness centrality (0.167) - this node is a cross-community bridge._
- **Why does `PosTerminalViewModel` connect `Login Screen` to `Top App Bar`, `Checkout ViewModel`, `PriceListDao`, `Item List Screen`, `Spacing Tokens`, `Bottom Nav & Stock Adjustment`, `PaymentMethodDto`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **Are the 28 inferred relationships involving `MainContent()` (e.g. with `AnalyticsScreen()` and `ChatScreen()`) actually correct?**
  _`MainContent()` has 28 INFERRED edges - model-reasoned connections that need verification._
- **What connects `@opencode-ai/plugin`, `Keys`, `ModuleInfoDto` to the rest of the system?**
  _191 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `UI State Components` be split into smaller, more focused modules?**
  _Cohesion score 0.05819209039548023 - nodes in this community are weakly interconnected._
- **Should `Cart & Checkout UI` be split into smaller, more focused modules?**
  _Cohesion score 0.05609756097560976 - nodes in this community are weakly interconnected._