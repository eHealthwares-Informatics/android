package com.ehealthwares.rxsoft.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class ReceiptLine(
    val name: String,
    val qty: BigDecimal,
    val price: BigDecimal,
    val total: BigDecimal
)

data class ReceiptData(
    val saleNumber: String,
    val customerName: String?,
    val items: List<ReceiptLine>,
    val subtotal: BigDecimal,
    val total: BigDecimal,
    val paidAmount: BigDecimal,
    val changeAmount: BigDecimal,
    val header: String? = null,
    val footer: String? = null
)

/** A4 page size in PostScript points (1/72"), used for the shareable PDF fallback. */
private const val PDF_PAGE_WIDTH = 595
private const val PDF_PAGE_HEIGHT = 842

private const val TAG = "ReceiptPrinter"

/**
 * Hand the receipt to the Android print framework. On devices where the print
 * spooler is disabled or missing, [PrintManager.print] throws
 * [android.content.ActivityNotFoundException] — it must never crash the POS
 * after a successful sale, so we degrade to generating a shareable PDF.
 */
fun printReceipt(context: Context, receipt: ReceiptData) {
    try {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager != null) {
            printManager.print("POS Receipt", ReceiptPrintAdapter(context, receipt), PrintAttributes.Builder().build())
            return
        }
        Log.w(TAG, "PrintManager unavailable; falling back to PDF share")
    } catch (e: Exception) {
        Log.w(TAG, "System print failed; falling back to PDF share", e)
    }
    shareReceiptPdf(context, receipt)
}

/**
 * Render the receipt to a PDF in cacheDir/receipts and open the system share
 * sheet (WhatsApp, email, Drive…). Never throws.
 */
fun shareReceiptPdf(context: Context, receipt: ReceiptData) {
    try {
        val document = PdfDocument()
        val page = document.startPage(
            PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create()
        )
        drawReceipt(page.canvas, page.canvas.width.toFloat(), receipt)
        document.finishPage(page)

        val dir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val safeSale = receipt.saleNumber.ifBlank { "sale" }.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val file = File(dir, "receipt-$safeSale.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share receipt"))
    } catch (e: Exception) {
        Log.e(TAG, "Could not generate or share receipt PDF", e)
        Toast.makeText(context, "Receipt could not be generated", Toast.LENGTH_SHORT).show()
    }
}

/** Draw the receipt onto a canvas of the given width (shared by print + PDF fallback). */
private fun drawReceipt(canvas: Canvas, width: Float, receipt: ReceiptData) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val x = 72f
    var y = 72f
    val textSize = 24f
    val smallTextSize = 18f
    val lineSpacing = 36f
    val format = NumberFormat.getCurrencyInstance(Locale("en", "NG"))

    paint.typeface = Typeface.MONOSPACE
    paint.textSize = textSize
    paint.color = Color.BLACK

    // Header
    paint.textSize = 28f
    paint.isFakeBoldText = true
    canvas.drawText("RxSoft", x, y, paint)
    y += lineSpacing + 10f
    paint.textSize = smallTextSize
    paint.isFakeBoldText = false
    canvas.drawText("Sale: ${receipt.saleNumber}", x, y, paint)
    y += lineSpacing
    receipt.customerName?.let {
        canvas.drawText("Customer: $it", x, y, paint)
        y += lineSpacing
    }

    // Separator
    y += 10f
    paint.textSize = smallTextSize
    canvas.drawLine(x, y, width - x, y, paint)
    y += lineSpacing

    // Items header
    paint.isFakeBoldText = true
    canvas.drawText("Item", x, y, paint)
    canvas.drawText("Qty", width * 0.6f, y, paint)
    canvas.drawText("Total", width * 0.8f, y, paint)
    paint.isFakeBoldText = false
    y += lineSpacing

    // Items
    paint.textSize = smallTextSize
    for (item in receipt.items) {
        val name = if (item.name.length > 25) item.name.take(24) + ".." else item.name
        canvas.drawText(name, x, y, paint)
        canvas.drawText(item.qty.toPlainString(), width * 0.6f, y, paint)
        canvas.drawText(format.format(item.total), width * 0.8f, y, paint)
        y += lineSpacing
    }

    // Totals
    canvas.drawLine(x, y, width - x, y, paint)
    y += lineSpacing + 8f
    paint.textSize = textSize
    paint.isFakeBoldText = true
    canvas.drawText("Total: ${format.format(receipt.total)}", x, y, paint)
    y += lineSpacing
    paint.isFakeBoldText = false
    paint.textSize = smallTextSize
    canvas.drawText("Paid: ${format.format(receipt.paidAmount)}", x, y, paint)
    y += lineSpacing
    if (receipt.changeAmount.compareTo(BigDecimal.ZERO) > 0) {
        canvas.drawText("Change: ${format.format(receipt.changeAmount)}", x, y, paint)
    }
}

private class ReceiptPrintAdapter(
    private val context: Context,
    private val receipt: ReceiptData
) : PrintDocumentAdapter() {

    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()
        val document = PrintedPdfDocument(context, attributes)
        try {
            val page = document.startPage(0)
            drawReceipt(page.canvas, page.canvas.width.toFloat(), receipt)
            document.finishPage(page)
            document.writeTo(FileOutputStream(destination.fileDescriptor))
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            Log.e(TAG, "Writing print document failed", e)
            callback.onWriteFailed(e.message)
        } finally {
            document.close()
        }
    }

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        metadata: Bundle?
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }
        val info = PrintDocumentInfo.Builder("receipt.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1)
            .build()
        callback.onLayoutFinished(info, true)
    }
}
