# Graph Report - rxsoft-mobile  (2026-08-19)

## Corpus Check
- 152 files · ~34,395 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1012 nodes · 1374 edges · 119 communities (79 shown, 40 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 124 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `9d4e4dfa`
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

## God Nodes (most connected - your core abstractions)
1. `PosTerminalViewModel` - 30 edges
2. `MainScaffold()` - 22 edges
3. `ListScreenTemplate()` - 19 edges
4. `Screen` - 19 edges
5. `UiState` - 19 edges
6. `ItemDto` - 18 edges
7. `EnterPinViewModel` - 18 edges
8. `PosRepository` - 17 edges
9. `NetworkModule` - 17 edges
10. `AuthViewModel` - 17 edges

## Surprising Connections (you probably didn't know these)
- `PosTerminalScreen()` --calls--> `ReceiptLine`  [INFERRED]
  app/src/main/java/com/rxsoft/mobile/ui/pos/PosTerminalScreen.kt → app/src/main/java/com/rxsoft/mobile/util/ReceiptPrinter.kt
- `ProductDetailScreen()` --references--> `Product`  [EXTRACTED]
  snippets/ProductDetailScreen.kt → app/src/main/java/com/rxsoft/mobile/ui/shop/model/Product.kt
- `CustomerSearchDialog()` --calls--> `PartyDto`  [INFERRED]
  app/src/main/java/com/rxsoft/mobile/ui/pos/PosTerminalScreen.kt → app/src/main/java/com/rxsoft/mobile/data/remote/dto/SaleDtos.kt
- `AppNavigation()` --calls--> `EnterPinScreen()`  [INFERRED]
  app/src/main/java/com/rxsoft/mobile/ui/navigation/AppNavigation.kt → app/src/main/java/com/rxsoft/mobile/ui/auth/EnterPinScreen.kt
- `LoginScreen()` --calls--> `AppLoadingState()`  [INFERRED]
  app/src/main/java/com/rxsoft/mobile/ui/auth/LoginScreen.kt → app/src/main/java/com/rxsoft/mobile/ui/design-system/components/AppLoading.kt

## Import Cycles
- None detected.

## Communities (119 total, 40 thin omitted)

### Community 0 - "Inventory Items API"
Cohesion: 0.08
Nodes (21): ItemsApi, List, Map, String, CreateItemRequest, OrgItemDto, PatchItemRequest, toItemDto() (+13 more)

### Community 1 - "UI State Components"
Cohesion: 0.27
Nodes (11): AppLinearProgress(), AppLoadingOverlay(), AppLoadingState(), AppPageLoading(), AppSkeletonLoader(), Boolean, Int, Modifier (+3 more)

### Community 2 - "Cart & Checkout UI"
Cohesion: 0.06
Nodes (32): CartItemCard(), CartItem, Modifier, AddProductCard(), CheckoutScreen(), EmptyCartCard(), CartItem, List (+24 more)

### Community 3 - "Stock Adjustment"
Cohesion: 0.07
Nodes (22): AdjustStockRequest, StockBalanceDto, StockLocationDto, InventoryRepository, Int, List, Result, String (+14 more)

### Community 4 - "Checkout ViewModel"
Cohesion: 0.06
Nodes (27): AddProductCard(), CheckoutScreen(), EmptyCartCard(), CheckoutViewModel, Double, Int, StateFlow, String (+19 more)

### Community 5 - "Medicine Catalog"
Cohesion: 0.07
Nodes (18): Int, List, StateFlow, String, ViewModel, MedicineCatalogViewModel, Product, Int (+10 more)

### Community 6 - "Sales Reports API"
Cohesion: 0.12
Nodes (12): Map, String, DailySalesReport, PaymentMethodSummary, TopSellingItem, List, Result, ReportsRepository (+4 more)

### Community 7 - "Item List Screen"
Cohesion: 0.09
Nodes (20): StateFlow, String, ViewModel, PosOrderDetailViewModel, PosOrderListScreen(), SaleCard(), Boolean, List (+12 more)

### Community 8 - "POS Sale Creation"
Cohesion: 0.10
Nodes (16): CreateSaleLine, CreateSalePayment, CreateSaleRequest, PartyDto, PaymentMethodDto, ReferenceDto, SaleLineDto, SalePaymentDto (+8 more)

### Community 9 - "PIN Entry Screen"
Cohesion: 0.11
Nodes (22): DeleteButton(), EnterPinScreen(), KeyButton(), Boolean, Int, String, PinBottomActions(), PinDots() (+14 more)

### Community 10 - "Customer API"
Cohesion: 0.09
Nodes (18): CustomersApi, Map, String, CreateCustomerRequest, CustomerDto, CustomerRepository, Int, List (+10 more)

### Community 11 - "App Navigation"
Cohesion: 0.11
Nodes (24): Bundle, MainActivity, AppNavigation(), Checkout, Customers, Inventory, ItemForm, Items (+16 more)

### Community 12 - "Authentication API"
Cohesion: 0.13
Nodes (10): AuthApi, AuthResponse, CurrentUserResponse, LoginRequest, ModuleInfoDto, RefreshRequest, AuthRepository, Boolean (+2 more)

### Community 13 - "Auth ViewModel"
Cohesion: 0.16
Nodes (11): AppScreen, AuthViewModel, StateFlow, String, Unit, ViewModel, Loading, Login (+3 more)

### Community 14 - "PIN Manager"
Cohesion: 0.19
Nodes (6): Boolean, Int, SharedPreferences, String, PinManager, StoredCredentials

### Community 15 - "Item Form ViewModel"
Cohesion: 0.15
Nodes (7): ItemFormState, ItemFormViewModel, Boolean, StateFlow, String, ViewModel, Uri

### Community 16 - "Receipt Printing"
Cohesion: 0.14
Nodes (14): Bundle, Context, printReceipt(), ReceiptData, ReceiptLine, ReceiptPrintAdapter, Array, CancellationSignal (+6 more)

### Community 17 - "Network & Payment Methods"
Cohesion: 0.17
Nodes (6): PaymentMethodsApi, UploadApi, Moshi, NetworkModule, OkHttpClient, Retrofit

### Community 18 - "Login Screen"
Cohesion: 0.36
Nodes (8): LoginScreen(), AppOutlinedButton(), AppPrimaryButton(), AppSecondaryButton(), AppTextButton(), Boolean, Modifier, String

### Community 19 - "Text Field & Item Form"
Cohesion: 0.17
Nodes (10): AppTextField(), Boolean, ImageVector, Modifier, String, Unit, StockAdjustmentScreen(), ImeAction (+2 more)

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
Cohesion: 0.17
Nodes (9): Modifier, String, ProductImage(), ImageVector, Modifier, String, RoundedIconButton(), String (+1 more)

### Community 25 - "Token Manager Module"
Cohesion: 0.05
Nodes (25): AuthInterceptor, Interceptor, Response, Interceptor, Response, ServerUrlInterceptor, Interceptor, Response (+17 more)

### Community 26 - "POS Terminal Search"
Cohesion: 0.39
Nodes (7): AppIconButton(), ImageVector, CartItemRow(), CustomerSearchDialog(), CartItem, PosTerminalScreen(), ProductSearchItem()

### Community 27 - "Settings ViewModel"
Cohesion: 0.27
Nodes (6): AppModule, Set, StateFlow, String, ViewModel, SettingsViewModel

### Community 28 - "Configuration API"
Cohesion: 0.22
Nodes (5): ConfigApi, OrganisationConfig, PriceListDto, PriceListItemDto, UserPosConfig

### Community 29 - "Bottom Nav & Stock Adjustment"
Cohesion: 0.38
Nodes (6): AppBottomNav(), BottomNavTab, List, Modifier, String, MainScaffold()

### Community 30 - "Prescription Upload"
Cohesion: 0.36
Nodes (8): BottomFloatingBar(), BrowseCard(), CreateRequestCard(), Int, Modifier, String, UploadCard(), UploadPrescriptionScreen()

### Community 31 - "Snackbar Component"
Cohesion: 0.25
Nodes (7): AppSnackbarHost(), Modifier, String, Unit, showInfo(), SnackbarDuration, SnackbarHostState

### Community 32 - "Session Manager"
Cohesion: 0.32
Nodes (3): Boolean, StateFlow, SessionManager

### Community 33 - "BigDecimal Adapter"
Cohesion: 0.47
Nodes (3): BigDecimalAdapter, BigDecimal, Double

### Community 34 - "Top App Bar"
Cohesion: 0.38
Nodes (6): AppTopAppBar(), AppTopAppBarActions(), ImageVector, Modifier, String, Unit

### Community 35 - "Prescription Upload Cards"
Cohesion: 0.43
Nodes (6): BrowseCard(), CreateRequestCard(), Int, String, UploadCard(), UploadPrescriptionScreen()

### Community 36 - "Pricing API"
Cohesion: 0.33
Nodes (3): Map, String, PricingApi

### Community 37 - "Profile Screen"
Cohesion: 0.47
Nodes (5): ImageVector, Int, String, ProfileMenuItem(), ProfileScreen()

### Community 38 - "Module Config"
Cohesion: 0.47
Nodes (4): AppModule, Set, StateFlow, ModuleConfig

### Community 39 - "Settings Screen"
Cohesion: 0.40
Nodes (5): Boolean, ImageVector, String, ModuleToggleCard(), SettingsScreen()

### Community 40 - "Server URL Manager"
Cohesion: 0.33
Nodes (5): AppSearchBar(), Modifier, String, MedicineCard(), MedicineCatalogScreen()

### Community 41 - "Image Upload API"
Cohesion: 0.15
Nodes (12): API Services, Architecture, Build Variants, Design System, Features, Layers, Navigation, Project Structure (+4 more)

### Community 42 - "Auth Interceptor"
Cohesion: 0.25
Nodes (6): ItemListViewModel, Boolean, List, StateFlow, String, ViewModel

### Community 43 - "Chip Component"
Cohesion: 0.40
Nodes (4): AppFilterChip(), Boolean, Modifier, String

### Community 44 - "Dialog Components"
Cohesion: 0.32
Nodes (6): AppAlertDialog(), AppInfoDialog(), Modifier, String, ItemFormScreen(), String

### Community 45 - "Badge Component"
Cohesion: 0.50
Nodes (3): AppBadge(), Int, Modifier

### Community 46 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 76 - "09-material3-expert.md"
Cohesion: 0.18
Nodes (10): Components, Icons, Layout, Mission, Motion, Never, Output, Principles (+2 more)

### Community 77 - "RxSoft Mobile Agent"
Cohesion: 0.20
Nodes (9): Adding a screen, API base URL, Architecture, Auth, Design system, Key commands, Overview, RxSoft Mobile Agent (+1 more)

### Community 78 - "ListScreenTemplate"
Cohesion: 0.20
Nodes (9): androidx, Boolean, Composable, List, Modifier, String, T, Unit (+1 more)

### Community 79 - "ListResponse"
Cohesion: 0.38
Nodes (3): InventoryApi, Map, String

### Community 83 - "10-android-animation-expert.md"
Cohesion: 0.22
Nodes (8): Durations, Healthcare, Mission, Never, Output, Performance, Preferred APIs, Use Cases

### Community 84 - "11-compose-testing-expert.md"
Cohesion: 0.22
Nodes (8): Accessibility, Assertions, Generate, Mission, Output, Performance, Test Tags, Verify

### Community 85 - "SalesApi"
Cohesion: 0.25
Nodes (4): Map, String, SalesApi, ListResponse

### Community 86 - "Compose Screen — rxsoft-mobile"
Cohesion: 0.25
Nodes (7): Compose Screen — rxsoft-mobile, Inputs, Purpose, Refactoring, When not to invoke, When to invoke, Workflow

### Community 87 - "18-form-engineer.md"
Cohesion: 0.25
Nodes (7): Generate, Input Components, Mission, Output, Principles, States, Validation

### Community 88 - "AppEmptyState"
Cohesion: 0.29
Nodes (6): AppEmptyState(), Composable, ImageVector, Modifier, String, Unit

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
Cohesion: 0.33
Nodes (8): AppCard(), AppOutlinedCard(), Modifier, String, Unit, DailySalesScreen(), ReportSummaryCard(), TopItemCard()

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
Cohesion: 0.25
Nodes (6): AppErrorState(), Modifier, String, Unit, String, PosOrderDetailScreen()

### Community 98 - "23-preview-generator.md"
Cohesion: 0.40
Nodes (4): Generate, Mission, Output, Preview Data

## Knowledge Gaps
- **154 isolated node(s):** `@opencode-ai/plugin`, `ModuleInfoDto`, `ListResponseMeta`, `ApiErrorResponse`, `ApiErrorDetail` (+149 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **40 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainScaffold()` connect `Bottom Nav & Stock Adjustment` to `AppErrorState`, `Stock Adjustment`, `Prescription Upload Cards`, `Profile Screen`, `Checkout ViewModel`, `Item List Screen`, `Settings Screen`, `Server URL Manager`, `Customer API`, `App Navigation`, `Dialog Components`, `Auth ViewModel`, `Medicine Catalog`, `LoginScreen`, `Text Field & Item Form`, `HTTP Logging Interceptor`, `POS Terminal Search`, `AppCard`?**
  _High betweenness centrality (0.147) - this node is a cross-community bridge._
- **Why does `NetworkModule` connect `Network & Payment Methods` to `Pricing API`, `Customer API`, `Authentication API`, `ListResponse`, `ReportsApi`, `SalesApi`, `Token Manager Module`, `Configuration API`?**
  _High betweenness centrality (0.117) - this node is a cross-community bridge._
- **Why does `ItemDto` connect `Inventory Items API` to `Stock Adjustment`, `POS Sale Creation`, `Auth Interceptor`, `LoginScreen`, `POS Terminal Search`?**
  _High betweenness centrality (0.094) - this node is a cross-community bridge._
- **Are the 18 inferred relationships involving `MainScaffold()` (e.g. with `CustomerListScreen()` and `AppBottomNav()`) actually correct?**
  _`MainScaffold()` has 18 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `ListScreenTemplate()` (e.g. with `CustomerListScreen()` and `AppEmptyState()`) actually correct?**
  _`ListScreenTemplate()` has 9 INFERRED edges - model-reasoned connections that need verification._
- **What connects `@opencode-ai/plugin`, `ModuleInfoDto`, `ListResponseMeta` to the rest of the system?**
  _154 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Inventory Items API` be split into smaller, more focused modules?**
  _Cohesion score 0.08418367346938775 - nodes in this community are weakly interconnected._