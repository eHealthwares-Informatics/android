# Graph Report - rxsoft-mobile  (2026-10-05)

## Corpus Check
- 214 files · ~88,160 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2037 nodes · 3526 edges · 173 communities (133 shown, 40 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 265 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ed5e4cb2`
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
- OfflineSyncManager
- ConversationListScreen
- .provideOkHttpClient
- PosOrderListViewModel
- ServerUrlManager
- SessionManager
- OrderDtos.kt
- ChatScreen
- AppearanceMode
- OrderLinesScreen
- BigDecimalAdapter
- ServerUrlInterceptor
- .parse
- AppBottomNav
- RxSoftTheme
- PosOrderDetailViewModel
- PosOrderListScreen
- ModuleConfig
- SyncSettingsManager
- make_launcher_icons.py
- AppAlertDialog
- AppBadge
- ColorTokens.kt
- LazyLinesAccordion
- RepositoryModule.kt
- ElevationTokens.kt
- MotionTokens.kt
- ShapeTokens.kt
- SpacingTokens.kt
- AppModule.kt
- Constants.kt

## God Nodes (most connected - your core abstractions)
1. `PosTerminalViewModel` - 52 edges
2. `OfflineDatabase` - 33 edges
3. `AuthViewModel` - 32 edges
4. `AppCard()` - 32 edges
5. `MainContent()` - 31 edges
6. `UiState` - 31 edges
7. `ItemFormViewModel` - 30 edges
8. `SettingsViewModel` - 30 edges
9. `Screen` - 27 edges
10. `PurchasesViewModel` - 27 edges

## Surprising Connections (you probably didn't know these)
- `CartItemCard()` --references--> `CartItem`  [EXTRACTED]
  snippets/CartItemCard.kt → app/src/main/java/com/ehealthwares/rxsoft/ui/pos/PosTerminalViewModel.kt
- `CheckoutScreen()` --references--> `CartItem`  [EXTRACTED]
  snippets/CheckoutScreen.kt → app/src/main/java/com/ehealthwares/rxsoft/ui/pos/PosTerminalViewModel.kt
- `CustomerSearchDialog()` --calls--> `PartyDto`  [INFERRED]
  app/src/main/java/com/ehealthwares/rxsoft/ui/pos/PosTerminalScreen.kt → app/src/main/java/com/ehealthwares/rxsoft/data/remote/dto/SaleDtos.kt
- `toItemDto()` --calls--> `ReferenceDto`  [INFERRED]
  app/src/main/java/com/ehealthwares/rxsoft/data/repository/PosRepository.kt → app/src/main/java/com/ehealthwares/rxsoft/data/remote/dto/SaleDtos.kt
- `AnalyticsScreen()` --calls--> `AppTopAppBar()`  [INFERRED]
  app/src/main/java/com/ehealthwares/rxsoft/ui/analytics/AnalyticsScreen.kt → app/src/main/java/com/ehealthwares/rxsoft/ui/design-system/components/AppTopAppBar.kt

## Import Cycles
- None detected.

## Communities (173 total, 40 thin omitted)

### Community 0 - "Inventory Items API"
Cohesion: 0.10
Nodes (16): Response, String, PrintDiscoveryApi, AgentInfo, Boolean, Int, List, String (+8 more)

### Community 1 - "UI State Components"
Cohesion: 0.06
Nodes (43): AppSideDrawer(), DrawerMenuItem, DrawerMenuSection, androidx, List, String, PharmacyMenuSections, Analytics (+35 more)

### Community 2 - "Cart & Checkout UI"
Cohesion: 0.06
Nodes (31): Product, CartItemCard(), Modifier, AddProductCard(), CheckoutScreen(), EmptyCartCard(), List, String (+23 more)

### Community 3 - "Stock Adjustment"
Cohesion: 0.07
Nodes (28): Map, String, PurchasesApi, UpdatePurchaseStatusRequest, CreatePurchaseLine, CreatePurchaseRequest, PurchaseDto, PurchaseLineDto (+20 more)

### Community 4 - "Checkout ViewModel"
Cohesion: 0.26
Nodes (5): CurrentUserResponse, AuthRepository, Boolean, Result, String

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
Cohesion: 0.06
Nodes (28): InventoryApi, Map, String, AdjustStockRequest, LotDto, StockBalanceDto, StockLocationDto, InventoryRepository (+20 more)

### Community 9 - "PIN Entry Screen"
Cohesion: 0.11
Nodes (18): DiscoveredPrinter, DiscoveryState, Error, Failure, Found, Idle, AppModule, Int (+10 more)

### Community 10 - "Customer API"
Cohesion: 0.16
Nodes (13): Entities, Boolean, com, Float, Int, List, Map, StateFlow (+5 more)

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
Cohesion: 0.11
Nodes (8): ItemFormState, ItemFormViewModel, Boolean, Job, StateFlow, String, ViewModel, Uri

### Community 16 - "Receipt Printing"
Cohesion: 0.20
Nodes (8): Bundle, Array, CancellationSignal, LayoutResultCallback, PageRange, ParcelFileDescriptor, PrintAttributes, WriteResultCallback

### Community 17 - "Network & Payment Methods"
Cohesion: 0.13
Nodes (5): ConfigApi, GenericProductsApi, PaymentMethodsApi, UploadApi, Retrofit

### Community 18 - "Login Screen"
Cohesion: 0.08
Nodes (23): CreateSaleLine, CreateSalePayment, PartyDto, SalePaymentDto, Created, CustomerQuickAddState, DuplicateFound, Failure (+15 more)

### Community 19 - "Text Field & Item Form"
Cohesion: 0.11
Nodes (15): AppTextField(), androidx, Boolean, ImageVector, Modifier, String, Unit, CreateOrderLineFields() (+7 more)

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
Cohesion: 0.13
Nodes (8): Flow, List, PriceListDao, CachedChatMessageEntity, CachedConversationEntity, CachedPriceEntity, CachedPriceListEntity, PendingOrderEntity

### Community 27 - "Settings ViewModel"
Cohesion: 0.11
Nodes (11): ChatStateStore, Keys, Flow, Int, String, LastOpen, AppModule, Context (+3 more)

### Community 28 - "Configuration API"
Cohesion: 0.17
Nodes (27): PurchasesAnalytics, SalesAnalytics, AnalyticsScreen(), CategoryRow(), formatNaira(), androidx, Double, Int (+19 more)

### Community 29 - "Bottom Nav & Stock Adjustment"
Cohesion: 0.22
Nodes (8): AppLog, Entry, Int, List, StateFlow, String, Char, Throwable

### Community 30 - "Prescription Upload"
Cohesion: 0.36
Nodes (8): BottomFloatingBar(), BrowseCard(), CreateRequestCard(), Int, Modifier, String, UploadCard(), UploadPrescriptionScreen()

### Community 31 - "Snackbar Component"
Cohesion: 0.18
Nodes (12): AppSnackbarHost(), Modifier, String, Unit, showInfo(), EditPriceDialog(), String, Unit (+4 more)

### Community 32 - "Session Manager"
Cohesion: 0.16
Nodes (10): ExchangeMessage, ChatRepository, ConversationEnded, Boolean, List, Map, SharedFlow, StateFlow (+2 more)

### Community 33 - "BigDecimal Adapter"
Cohesion: 0.10
Nodes (9): CachedChatMessageDao, androidx, migrate(), OfflineDatabase, PendingOrderDao, PriceDao, Context, OfflineModule (+1 more)

### Community 34 - "Top App Bar"
Cohesion: 0.05
Nodes (37): ItemsApi, Int, List, Map, String, Map, String, SalesApi (+29 more)

### Community 35 - "Prescription Upload Cards"
Cohesion: 0.27
Nodes (5): AuthApi, AuthResponse, LoginRequest, ModuleInfoDto, RefreshRequest

### Community 36 - "Pricing API"
Cohesion: 0.13
Nodes (12): List, String, PaymentsApi, AvailablePaymentProviderDto, InitializePaymentRequest, InitializePaymentResponse, PaymentProviderRef, VerifyPaymentResponse (+4 more)

### Community 37 - "Profile Screen"
Cohesion: 0.15
Nodes (22): AppIconButton(), AppOutlinedButton(), AppPrimaryButton(), AppSecondaryButton(), AppTextButton(), Boolean, ImageVector, Modifier (+14 more)

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

### Community 43 - "Chip Component"
Cohesion: 0.21
Nodes (9): GenericProductDto, Int, List, Result, String, OrdersRepository, OrderSubmitResult, Pushed (+1 more)

### Community 44 - "Dialog Components"
Cohesion: 0.17
Nodes (10): ChatApi, Boolean, Int, Response, String, Unit, ConversationInboxResponse, ExchangeMessagesResponse (+2 more)

### Community 45 - "Badge Component"
Cohesion: 0.22
Nodes (11): DailySalesRow, DateRangeFilterRow(), DateRangePreset, List, String, DailyRowCard(), DailySalesScreen(), List (+3 more)

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
Cohesion: 0.20
Nodes (6): Response, String, Unit, PrintApi, ReceiptPrintRequest, ReceiptLine

### Community 50 - "Cart Item Model"
Cohesion: 0.21
Nodes (9): ChatViewModel, ConversationListViewModel, Boolean, List, Map, SharedFlow, StateFlow, String (+1 more)

### Community 51 - "Theme Definition"
Cohesion: 0.17
Nodes (10): List, String, WebsiteApi, OrderDto, Unit, OrderCard(), OrderDetailDialog(), OrderLineRow() (+2 more)

### Community 52 - "Repository Module"
Cohesion: 0.34
Nodes (14): AppEmptyStateDashboard(), AppErrorStateDashboard(), AppLoadingStateDashboard(), AppointmentRow(), DashboardKpiCard(), DashboardQuickAction(), Color, ImageVector (+6 more)

### Community 53 - "Elevation Tokens"
Cohesion: 0.06
Nodes (31): List, Map, Response, ResponseBody, String, ReportsApi, PaymentMethodSummary, PurchasesAnalyticsSummary (+23 more)

### Community 54 - "Motion Tokens"
Cohesion: 0.33
Nodes (3): Moshi, NetworkModule, OkHttpClient

### Community 55 - "Shape Tokens"
Cohesion: 0.22
Nodes (13): ColorThemeSection(), CustomColorSection(), Boolean, Color, Long, Modifier, String, Unit (+5 more)

### Community 56 - "Spacing Tokens"
Cohesion: 0.16
Nodes (20): AppSearchBar(), Modifier, String, CartItemRow(), CustomerQuickAddDialog(), CustomerSearchDialog(), androidx, Boolean (+12 more)

### Community 57 - "App Module"
Cohesion: 0.18
Nodes (3): CustomerDao, Int, CachedCustomerEntity

### Community 58 - "Constants"
Cohesion: 0.31
Nodes (5): Boolean, String, PaymentAppDetector, PaymentProviderType, Intent

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
Cohesion: 0.42
Nodes (8): drawReceipt(), Context, Float, printReceipt(), ReceiptData, ReceiptPrintAdapter, shareReceiptPdf(), PrintDocumentAdapter

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
Nodes (10): AppLinearProgress(), AppLoadingOverlay(), AppPageLoading(), AppSkeletonLoader(), Boolean, Int, Modifier, String (+2 more)

### Community 86 - "Compose Screen — rxsoft-mobile"
Cohesion: 0.25
Nodes (7): Compose Screen — rxsoft-mobile, Inputs, Purpose, Refactoring, When not to invoke, When to invoke, Workflow

### Community 87 - "18-form-engineer.md"
Cohesion: 0.25
Nodes (7): Generate, Input Components, Mission, Output, Principles, States, Validation

### Community 88 - "AppEmptyState"
Cohesion: 0.24
Nodes (13): DiscoveryResults(), formatSyncTime(), Boolean, ImageVector, List, Long, String, Unit (+5 more)

### Community 89 - "21-healthcare-ux.md"
Cohesion: 0.29
Nodes (6): Mission, Never, Optimize, Output, Principles, Users

### Community 90 - "22-state-management.md"
Cohesion: 0.29
Nodes (6): Avoid, Generate, Mission, Output, Principles, Support

### Community 91 - "24-theme-customization.md"
Cohesion: 0.29
Nodes (6): Architecture, Generate, Mission, Output, Persistence, Support

### Community 93 - "AppCard"
Cohesion: 0.12
Nodes (16): AppEmptyState(), ImageVector, Modifier, String, Unit, AppErrorState(), Modifier, String (+8 more)

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
Cohesion: 0.13
Nodes (14): 4a. Cache Payment Methods, 4b. Offline Stock Adjustments, 4c. Update OfflineSyncManager, Execution Order, Fix log — 2026-09-29 (all implemented, built 13:52, installed on SM-A055F), Issue 1: Product Creation — Missing Required Backend Parameters, Issue 1: UOM dropdown empty on New Item — FIXED, Issue 2: Receipt crash after POS sale — FIXED (+6 more)

### Community 122 - "OfflineItemDao"
Cohesion: 0.13
Nodes (5): CachedConversationDao, String, OfflineItemDao, CachedItemEntity, CatalogItemWithPrice

### Community 123 - "PendingStockAdjustmentDao"
Cohesion: 0.18
Nodes (3): Long, PendingStockAdjustmentDao, PendingStockAdjustmentEntity

### Community 125 - "ListResponse"
Cohesion: 0.43
Nodes (7): Field(), String, Unit, PosConfigCard(), UserDetailScreen(), UserFieldsCard(), UserHeader()

### Community 126 - "PaymentMethodDto"
Cohesion: 0.38
Nodes (3): UserPosConfig, StateFlow, PosConfigManager

### Community 127 - "AppTopAppBar"
Cohesion: 0.19
Nodes (12): AppTopAppBar(), AppTopAppBarActions(), ImageVector, Modifier, String, Unit, ImageVector, Int (+4 more)

### Community 128 - "SimpleBarChart"
Cohesion: 0.27
Nodes (10): BarChartData, drawBarChart(), Color, Double, Int, List, Modifier, String (+2 more)

### Community 129 - "MainContent"
Cohesion: 0.40
Nodes (3): StateFlow, ViewModel, UserDetailViewModel

### Community 130 - "CheckoutScreen"
Cohesion: 0.50
Nodes (3): Interceptor, Response, TokenRefreshInterceptor

### Community 131 - "ThemeViewModel"
Cohesion: 0.22
Nodes (5): AndroidViewModel, Long, StateFlow, ThemeSettings, ThemeViewModel

### Community 133 - "PriceListDao"
Cohesion: 0.70
Nodes (4): EmptyListMessage(), ItemFormScreen(), String, Unit

### Community 136 - ".items"
Cohesion: 0.11
Nodes (17): CustomerCard(), CustomerListScreen(), Unit, Modifier, RxAppBackdrop(), RxBackdropStyle, Unit, StockAdjustmentScreen() (+9 more)

### Community 138 - "ThemePreferences"
Cohesion: 0.22
Nodes (5): Keys, Flow, Long, ThemeSettings, ThemePreferences

### Community 139 - "ItemListViewModel"
Cohesion: 0.25
Nodes (6): ItemListViewModel, Boolean, List, StateFlow, String, ViewModel

### Community 141 - "OfflineSyncManager"
Cohesion: 0.23
Nodes (5): Boolean, Job, Long, StateFlow, OfflineSyncManager

### Community 142 - "ConversationListScreen"
Cohesion: 0.32
Nodes (7): ConversationInboxItem, ConversationItem(), ConversationListScreen(), Modifier, String, Unit, toRelativeTime()

### Community 143 - ".provideOkHttpClient"
Cohesion: 0.40
Nodes (3): AuthInterceptor, Interceptor, Response

### Community 145 - "PosOrderListViewModel"
Cohesion: 0.25
Nodes (5): Boolean, List, StateFlow, ViewModel, PosOrderListViewModel

### Community 147 - "ServerUrlManager"
Cohesion: 0.24
Nodes (5): Boolean, SharedPreferences, String, Mode, ServerUrlManager

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
Cohesion: 0.23
Nodes (11): ReceiptPrintItem, Failure, Idle, List, StateFlow, String, ViewModel, PosOrderDetailViewModel (+3 more)

### Community 160 - "PosOrderListScreen"
Cohesion: 0.31
Nodes (10): formatSaleDate(), androidx, Boolean, String, Unit, PosOrderListScreen(), SaleCard(), StatusChip() (+2 more)

### Community 162 - "ModuleConfig"
Cohesion: 0.47
Nodes (4): AppModule, Set, StateFlow, ModuleConfig

### Community 164 - "SyncSettingsManager"
Cohesion: 0.40
Nodes (4): Flow, Int, Long, SyncSettingsManager

### Community 165 - "make_launcher_icons.py"
Cohesion: 0.60
Nodes (5): load_logo(), main(), make_background(), make_foreground(), make_legacy()

### Community 166 - "AppAlertDialog"
Cohesion: 0.60
Nodes (4): AppAlertDialog(), AppInfoDialog(), Modifier, String

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
- **194 isolated node(s):** `@opencode-ai/plugin`, `Keys`, `ModuleInfoDto`, `ListResponseMeta`, `ApiErrorResponse` (+189 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **40 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainContent()` connect `.items` to `UI State Components`, `PriceListDao`, `Sales Reports API`, `PIN Entry Screen`, `ConversationListScreen`, `ChatScreen`, `OrderLinesScreen`, `Configuration API`, `Snackbar Component`, `PosOrderListScreen`, `Profile Screen`, `Badge Component`, `Mobile Theme`, `Theme Definition`, `Shape Tokens`, `Spacing Tokens`, `AppEmptyState`, `AppCard`, `ListResponse`, `AppTopAppBar`?**
  _High betweenness centrality (0.158) - this node is a cross-community bridge._
- **Why does `UiState` connect `Item List Screen` to `MainContent`, `Stock Adjustment`, `Medicine Catalog`, `Sales Reports API`, `POS Sale Creation`, `ItemListViewModel`, `PIN Manager`, `PosOrderListViewModel`, `Login Screen`, `Text Field & Item Form`, `HTTP Logging Interceptor`, `Configuration API`, `PosOrderDetailViewModel`, `Profile Screen`, `Module Config`, `Settings Screen`, `Mobile Theme`, `Elevation Tokens`, `ReportsApi`, `PriceListViewModel`, `PaymentMethodDto`?**
  _High betweenness centrality (0.151) - this node is a cross-community bridge._
- **Why does `SettingsViewModel` connect `PIN Entry Screen` to `Inventory Items API`, `OfflineDatabase.kt`, `.items`, `Customer API`, `ServerUrlManager`, `AppEmptyState`?**
  _High betweenness centrality (0.095) - this node is a cross-community bridge._
- **Are the 28 inferred relationships involving `AppCard()` (e.g. with `CategoryRow()` and `LocationRow()`) actually correct?**
  _`AppCard()` has 28 INFERRED edges - model-reasoned connections that need verification._
- **What connects `@opencode-ai/plugin`, `Keys`, `ModuleInfoDto` to the rest of the system?**
  _194 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Inventory Items API` be split into smaller, more focused modules?**
  _Cohesion score 0.10416666666666667 - nodes in this community are weakly interconnected._
- **Should `UI State Components` be split into smaller, more focused modules?**
  _Cohesion score 0.06334841628959276 - nodes in this community are weakly interconnected._