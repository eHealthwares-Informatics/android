package com.rxsoft.mobile.util

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
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

fun printReceipt(context: Context, receipt: ReceiptData) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val printAdapter = ReceiptPrintAdapter(context, receipt)
    printManager.print("POS Receipt", printAdapter, PrintAttributes.Builder().build())
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
        val page = document.startPage(0)
        val canvas = page.canvas
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
        canvas.drawLine(x, y, canvas.width - x, y, paint)
        y += lineSpacing

        // Items header
        paint.isFakeBoldText = true
        canvas.drawText("Item", x, y, paint)
        canvas.drawText("Qty", canvas.width * 0.6f, y, paint)
        canvas.drawText("Total", canvas.width * 0.8f, y, paint)
        paint.isFakeBoldText = false
        y += lineSpacing

        // Items
        paint.textSize = smallTextSize
        for (item in receipt.items) {
            val name = if (item.name.length > 25) item.name.take(24) + ".." else item.name
            canvas.drawText(name, x, y, paint)
            canvas.drawText(item.qty.toPlainString(), canvas.width * 0.6f, y, paint)
            canvas.drawText(format.format(item.total), canvas.width * 0.8f, y, paint)
            y += lineSpacing
        }

        // Totals
        canvas.drawLine(x, y, canvas.width - x, y, paint)
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

        document.finishPage(page)

        try {
            document.writeTo(FileOutputStream(destination.fileDescriptor))
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
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
