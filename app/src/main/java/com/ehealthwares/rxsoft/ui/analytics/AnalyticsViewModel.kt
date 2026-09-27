package com.rxsoft.mobile.ui.analytics

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.OrderDto
import com.rxsoft.mobile.data.remote.dto.PurchasesAnalytics
import com.rxsoft.mobile.data.remote.dto.SalesAnalytics
import com.rxsoft.mobile.data.repository.OrdersRepository
import com.rxsoft.mobile.data.repository.ReportsRepository
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/** Lightweight per-tab analytics summary derived from the raw endpoint data. */
data class OrderAnalyticsSummary(
    val totalOrders: Int = 0,
    val totalValue: BigDecimal = BigDecimal.ZERO,
    val pendingOrders: Int = 0,
    val postedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val recentOrders: List<OrderDto> = emptyList(),
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository,
    private val ordersRepository: OrdersRepository,
) : ViewModel() {

    private val _salesAnalytics = MutableStateFlow<UiState<SalesAnalytics>>(UiState.Loading)
    val salesAnalytics: StateFlow<UiState<SalesAnalytics>> = _salesAnalytics.asStateFlow()

    private val _purchasesAnalytics = MutableStateFlow<UiState<PurchasesAnalytics>>(UiState.Loading)
    val purchasesAnalytics: StateFlow<UiState<PurchasesAnalytics>> = _purchasesAnalytics.asStateFlow()

    private val _orderAnalytics = MutableStateFlow<UiState<OrderAnalyticsSummary>>(UiState.Loading)
    val orderAnalytics: StateFlow<UiState<OrderAnalyticsSummary>> = _orderAnalytics.asStateFlow()

    // Date-range filter state
    private val _fromDate = MutableStateFlow<String?>(null)
    val fromDate: StateFlow<String?> = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow<String?>(null)
    val toDate: StateFlow<String?> = _toDate.asStateFlow()

    // Export state
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        loadAll()
    }

    /** Set the from/to date range and refresh both sales + purchases analytics. */
    fun setDateRange(from: LocalDate?, to: LocalDate?) {
        _fromDate.value = from?.format(fmt)
        _toDate.value = to?.format(fmt)
        loadSalesAnalytics()
        loadPurchasesAnalytics()
    }

    /** Clear both dates and refresh. */
    fun clearDateRange() {
        _fromDate.value = null
        _toDate.value = null
        loadSalesAnalytics()
        loadPurchasesAnalytics()
    }

    fun loadAll() {
        loadSalesAnalytics()
        loadPurchasesAnalytics()
        loadOrderAnalytics()
    }

    private fun loadSalesAnalytics() {
        viewModelScope.launch {
            _salesAnalytics.value = UiState.Loading
            reportsRepository.getSalesAnalytics(
                from = _fromDate.value,
                to = _toDate.value,
            )
                .onSuccess { _salesAnalytics.value = UiState.Success(it) }
                .onFailure { e ->
                    Log.e("AnalyticsVM", "Failed to load sales analytics: ${e.message}", e)
                    _salesAnalytics.value = UiState.Error(e.message ?: "Failed to load sales analytics")
                }
        }
    }

    private fun loadPurchasesAnalytics() {
        viewModelScope.launch {
            _purchasesAnalytics.value = UiState.Loading
            reportsRepository.getPurchasesAnalytics(
                from = _fromDate.value,
                to = _toDate.value,
            )
                .onSuccess { _purchasesAnalytics.value = UiState.Success(it) }
                .onFailure { e ->
                    Log.e("AnalyticsVM", "Failed to load purchases analytics: ${e.message}", e)
                    _purchasesAnalytics.value = UiState.Error(e.message ?: "Failed to load purchases analytics")
                }
        }
    }

    private fun loadOrderAnalytics() {
        viewModelScope.launch {
            _orderAnalytics.value = UiState.Loading
            ordersRepository.listOrders()
                .onSuccess { orders ->
                    val pending = orders.count { it.orderStatus == "pending" || it.orderStatus == "draft" }
                    val posted = orders.count { it.orderStatus == "posted" || it.orderStatus == "completed" || it.orderStatus == "sale" }
                    val cancelled = orders.count { it.orderStatus == "cancelled" }
                    _orderAnalytics.value = UiState.Success(
                        OrderAnalyticsSummary(
                            totalOrders = orders.size,
                            totalValue = orders.sumOf { it.totalAmount },
                            pendingOrders = pending,
                            postedOrders = posted,
                            cancelledOrders = cancelled,
                            recentOrders = orders.take(10),
                        )
                    )
                }
                .onFailure { e ->
                    Log.e("AnalyticsVM", "Failed to load orders: ${e.message}", e)
                    _orderAnalytics.value = UiState.Error(e.message ?: "Failed to load orders")
                }
        }
    }

    /** Refresh the currently-selected tab data. */
    fun refresh(tabIndex: Int) {
        when (tabIndex) {
            0 -> loadSalesAnalytics()
            1 -> loadPurchasesAnalytics()
            2 -> loadOrderAnalytics()
        }
    }

    fun refreshAll() = loadAll()

    fun clearExportMessage() { _exportMessage.value = null }

    /** Convenience: date-range strings for the UI. */
    val fromDisplay: String
        get() = _fromDate.value?.let { LocalDate.parse(it, fmt) }?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "All"

    val toDisplay: String
        get() = _toDate.value?.let { LocalDate.parse(it, fmt) }?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "All"

    // ── Export helpers ──────────────────────────────────────────────────────

    /** Export CSV from the server and save to Downloads folder. */
    fun exportCsv(context: Context) {
        viewModelScope.launch {
            _isExporting.value = true
            _exportMessage.value = null
            withContext(Dispatchers.IO) {
                reportsRepository.exportCsv(from = _fromDate.value, to = _toDate.value)
                    .map { body ->
                        val filename = "rxsoft_analytics_${System.currentTimeMillis()}.csv"
                        val inputStream = body.byteStream()
                        val uri = saveToDownloads(context, filename, "text/csv") { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        uri?.toString() ?: throw Exception("Failed to save file")
                    }
            }.onSuccess {
                _exportMessage.value = "CSV saved to Downloads"
            }.onFailure { e ->
                _exportMessage.value = "Export failed: ${e.message}"
            }
            _isExporting.value = false
        }
    }

    /** Generate a PDF from the current analytics data and save to Downloads. */
    fun exportPdf(context: Context) {
        viewModelScope.launch {
            _isExporting.value = true
            _exportMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    val format = NumberFormat.getCurrencyInstance(Locale("en", "NG"))
                    val document = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
                    val page = document.startPage(pageInfo)
                    val canvas: Canvas = page.canvas
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                    val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFakeBoldText = true }
                    var y = 60f
                    val x = 40f
                    val lineHeight = 28f

                    // Title
                    boldPaint.textSize = 20f
                    boldPaint.color = Color.BLACK
                    canvas.drawText("RxSoft Analytics Report", x, y, boldPaint)
                    y += lineHeight + 8f

                    paint.textSize = 11f
                    paint.color = Color.DKGRAY
                    val dateRange = if (_fromDate.value != null || _toDate.value != null) {
                        "${_fromDate.value ?: "Start"} to ${_toDate.value ?: "Now"}"
                    } else "All time"
                    canvas.drawText("Period: $dateRange", x, y, paint)
                    y += lineHeight
                    canvas.drawText("Generated: ${LocalDate.now()}", x, y, paint)
                    y += lineHeight + 12f

                    // Separator
                    paint.color = Color.LTGRAY
                    canvas.drawLine(x, y, 555f, y, paint)
                    y += lineHeight

                    // ── Sales section ────────────────────────────────────────
                    boldPaint.textSize = 14f
                    boldPaint.color = Color.BLACK
                    canvas.drawText("Sales", x, y, boldPaint)
                    y += lineHeight
                    paint.textSize = 11f
                    paint.color = Color.DKGRAY

                    val salesData = (_salesAnalytics.value as? UiState.Success)?.data?.summary
                    if (salesData != null) {
                        canvas.drawText("Revenue: ${format.format(salesData.totalRevenue)}", x + 10, y, paint); y += lineHeight
                        canvas.drawText("Transactions: ${salesData.totalSales}", x + 10, y, paint); y += lineHeight
                        canvas.drawText("Avg Order: ${format.format(salesData.averageOrderValue)}", x + 10, y, paint); y += lineHeight
                        canvas.drawText("Items Sold: ${salesData.itemsSold}", x + 10, y, paint); y += lineHeight
                    } else {
                        canvas.drawText("No sales data", x + 10, y, paint); y += lineHeight
                    }
                    y += 8f

                    // ── Purchases section ───────────────────────────────────
                    boldPaint.textSize = 14f
                    canvas.drawText("Purchases", x, y, boldPaint)
                    y += lineHeight
                    paint.textSize = 11f

                    val purchaseData = (_purchasesAnalytics.value as? UiState.Success)?.data?.summary
                    if (purchaseData != null) {
                        canvas.drawText("Total Cost: ${format.format(purchaseData.totalValue)}", x + 10, y, paint); y += lineHeight
                        canvas.drawText("Orders: ${purchaseData.totalPOs}", x + 10, y, paint); y += lineHeight
                        canvas.drawText("Avg Order: ${format.format(purchaseData.averagePOValue)}", x + 10, y, paint); y += lineHeight
                        canvas.drawText("Items Bought: ${purchaseData.itemsPurchased}", x + 10, y, paint); y += lineHeight
                        purchaseData.topSupplier?.let {
                            canvas.drawText("Top Supplier: ${it.name ?: "-"} (${format.format(it.value)})", x + 10, y, paint)
                            y += lineHeight
                        }
                    } else {
                        canvas.drawText("No purchase data", x + 10, y, paint); y += lineHeight
                    }
                    y += 8f

                    // ── Orders section ──────────────────────────────────────
                    boldPaint.textSize = 14f
                    canvas.drawText("Orders", x, y, boldPaint)
                    y += lineHeight
                    paint.textSize = 11f

                    val orderData = (_orderAnalytics.value as? UiState.Success)?.data
                    if (orderData != null && orderData.totalOrders > 0) {
                        canvas.drawText("Total Orders: ${orderData.totalOrders}", x + 10, y, paint); y += lineHeight
                        canvas.drawText("Total Value: ${format.format(orderData.totalValue)}", x + 10, y, paint); y += lineHeight
                        canvas.drawText("Pending: ${orderData.pendingOrders}  Posted: ${orderData.postedOrders}  Cancelled: ${orderData.cancelledOrders}", x + 10, y, paint)
                        y += lineHeight
                    } else {
                        canvas.drawText("No order data", x + 10, y, paint); y += lineHeight
                    }

                    // ── Trend table ─────────────────────────────────────────
                    y += 16f
                    paint.color = Color.LTGRAY
                    canvas.drawLine(x, y, 555f, y, paint)
                    y += lineHeight
                    boldPaint.textSize = 14f
                    boldPaint.color = Color.BLACK
                    canvas.drawText("Daily Trend", x, y, boldPaint)
                    y += lineHeight
                    boldPaint.textSize = 10f
                    canvas.drawText("Day", x, y, boldPaint)
                    canvas.drawText("Revenue/Cost", x + 200, y, boldPaint)
                    canvas.drawText("Count", x + 380, y, boldPaint)
                    y += lineHeight - 4f
                    paint.textSize = 10f
                    paint.color = Color.DKGRAY

                    val trendRows = (_salesAnalytics.value as? UiState.Success)?.data?.trend.orEmpty()
                    trendRows.take(30).forEach { point ->
                        canvas.drawText(point.day, x, y, paint)
                        canvas.drawText(format.format(point.revenue), x + 200, y, paint)
                        canvas.drawText(point.orders.toString(), x + 380, y, paint)
                        y += lineHeight - 4f
                    }
                    if (trendRows.size > 30) {
                        canvas.drawText("... and ${trendRows.size - 30} more days", x, y, paint)
                    }

                    document.finishPage(page)

                    val filename = "rxsoft_analytics_${System.currentTimeMillis()}.pdf"
                    val uri = saveToDownloads(context, filename, "application/pdf") { outputStream ->
                        document.writeTo(outputStream)
                    }
                    document.close()

                    uri ?: throw Exception("Failed to save PDF")
                }
                _exportMessage.value = "PDF saved to Downloads"
            } catch (e: Exception) {
                Log.e("AnalyticsVM", "Failed to export PDF: ${e.message}", e)
                _exportMessage.value = "Export failed: ${e.message}"
            }
            _isExporting.value = false
        }
    }

    /** Save a file to the Downloads folder via MediaStore. */
    private fun saveToDownloads(
        context: Context,
        filename: String,
        mimeType: String,
        write: (java.io.OutputStream) -> Unit,
    ): android.net.Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
        }
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, contentValues) ?: return null
        resolver.openOutputStream(uri)?.use { write(it) }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
        return uri
    }
}
