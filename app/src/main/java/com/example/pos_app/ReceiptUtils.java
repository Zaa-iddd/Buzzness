package com.example.pos_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import com.example.pos_app.data.Transaction;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class ReceiptUtils {

    public static void printTransactionReceipt(Context context, Transaction transaction) {
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        String jobName = context.getString(R.string.app_name) + " Receipt";

        printManager.print(jobName, new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                    return;
                }

                PrintDocumentInfo info = new PrintDocumentInfo.Builder("receipt_" + transaction.id + ".pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build();
                callback.onLayoutFinished(info, true);
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                PdfDocument pdfDocument = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                
                int y = 40;
                paint.setTextSize(18f);
                paint.setFakeBoldText(true);
                canvas.drawText("STORE RECEIPT", 80, y, paint);
                
                y += 30;
                paint.setTextSize(12f);
                paint.setFakeBoldText(false);
                canvas.drawText("Date: " + new Date(transaction.timestamp).toString(), 20, y, paint);
                
                y += 20;
                canvas.drawLine(20, y, 280, y, paint);
                
                y += 30;
                // Parse items from description if they follow "POS Sale: Item1 x1, Item2 x2, "
                String content = transaction.description;
                if (content.startsWith("POS Sale: ")) {
                    content = content.substring(10);
                }
                
                String[] items = content.split(", ");
                for (String item : items) {
                    if (item.trim().isEmpty()) continue;
                    canvas.drawText(item, 20, y, paint);
                    y += 20;
                    if (y > 500) break; // Simple overflow check
                }
                
                y += 10;
                canvas.drawLine(20, y, 280, y, paint);
                y += 30;
                paint.setFakeBoldText(true);
                paint.setTextSize(14f);
                canvas.drawText("TOTAL:", 20, y, paint);
                canvas.drawText(String.format(Locale.getDefault(), "$%.2f", transaction.amount), 210, y, paint);
                
                y += 40;
                paint.setTextSize(10f);
                paint.setFakeBoldText(false);
                canvas.drawText("Thank you for your business!", 60, y, paint);

                pdfDocument.finishPage(page);

                try {
                    pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
                } catch (IOException e) {
                    callback.onWriteFailed(e.toString());
                    return;
                } finally {
                    pdfDocument.close();
                }
                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            }
        }, null);
    }
}
