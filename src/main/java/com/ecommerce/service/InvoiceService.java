package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceService {

    public byte[] generatePdf(Order order) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page, AppendMode.APPEND, true, true)) {
                float y = 770;
                y = writeLine(content, "Invoice", 18, 50, y);
                y -= 18;
                y = writeLine(content, "Order Number: " + order.getOrderNumber(), 11, 50, y);
                y -= 12;
                y = writeLine(content, "Order Date: " + DateTimeFormatter.ISO_INSTANT.format(order.getCreatedAt()), 11, 50, y);
                y -= 12;
                y = writeLine(content, "Recipient: " + order.getRecipientName(), 11, 50, y);
                y -= 12;
                y = writeLine(content, "Shipping Address: " + order.getShippingAddress(), 11, 50, y);
                y -= 12;
                y = writeLine(content, "Status: " + order.getStatus().name(), 11, 50, y);
                y -= 16;
                y = writeLine(content, "Items:", 12, 50, y);

                content.setFont(PDType1Font.HELVETICA, 10);
                for (OrderItem item : order.getItems()) {
                    String line = item.getProductName() + " x" + item.getQuantity()
                            + " @ " + money(item.getPrice())
                            + " = " + money(item.getSubtotal());
                    y -= 14;
                    content.beginText();
                    content.newLineAtOffset(50, y);
                    content.showText(sanitize(line));
                    content.endText();
                }

                BigDecimal total = order.getTotalAmount();
                y -= 20;
                content.beginText();
                content.newLineAtOffset(50, y);
                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                content.showText("Total: " + money(total));
                content.endText();
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate invoice", exception);
        }
    }

    private float writeLine(PDPageContentStream content, String text, int fontSize, float x, float y) throws IOException {
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(sanitize(text));
        content.endText();
        return y;
    }

    private String money(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String sanitize(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
