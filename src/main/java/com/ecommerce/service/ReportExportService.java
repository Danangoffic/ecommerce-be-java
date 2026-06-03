package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public byte[] exportCsv(Integer days) {
        List<ReportRow> rows = buildRows(days);
        StringBuilder builder = new StringBuilder();
        builder.append("date,orderNumber,status,totalAmount,itemCount,recipientName\n");
        for (ReportRow row : rows) {
            builder.append(row.date()).append(',')
                    .append(row.orderNumber()).append(',')
                    .append(row.status()).append(',')
                    .append(row.totalAmount()).append(',')
                    .append(row.itemCount()).append(',')
                    .append(escape(row.recipientName()))
                    .append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportExcel(Integer days) {
        List<ReportRow> rows = buildRows(days);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sales Report");
            String[] headers = {"date", "orderNumber", "status", "totalAmount", "itemCount", "recipientName"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowIndex = 1;
            for (ReportRow row : rows) {
                Row excelRow = sheet.createRow(rowIndex++);
                excelRow.createCell(0).setCellValue(row.date().toString());
                excelRow.createCell(1).setCellValue(row.orderNumber());
                excelRow.createCell(2).setCellValue(row.status());
                excelRow.createCell(3).setCellValue(row.totalAmount().doubleValue());
                excelRow.createCell(4).setCellValue(row.itemCount());
                excelRow.createCell(5).setCellValue(row.recipientName());
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to export report", exception);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportPdf(Integer days) {
        List<ReportRow> rows = buildRows(days);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = 760;
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 16);
                content.newLineAtOffset(50, y);
                content.showText("Sales Report");
                content.endText();
                y -= 24;
                content.setFont(PDType1Font.HELVETICA, 10);
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("date | order | status | amount | items | recipient");
                content.endText();
                for (ReportRow row : rows) {
                    y -= 14;
                    content.beginText();
                    content.newLineAtOffset(50, y);
                    content.showText(sanitize(row.date() + " | " + row.orderNumber() + " | " + row.status() + " | " + money(row.totalAmount()) + " | " + row.itemCount() + " | " + row.recipientName()));
                    content.endText();
                }
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to export report", exception);
        }
    }

    private List<ReportRow> buildRows(Integer days) {
        int resolvedDays = days == null || days < 1 ? 30 : Math.min(days, 365);
        Instant to = Instant.now();
        Instant from = to.minus(resolvedDays - 1L, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        List<Order> orders = orderRepository.findDetailedByCreatedAtBetween(from, to);
        List<ReportRow> rows = new ArrayList<>();
        for (Order order : orders) {
            rows.add(new ReportRow(
                    order.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                    order.getOrderNumber(),
                    order.getStatus().name(),
                    order.getTotalAmount(),
                    order.getItems().size(),
                    order.getRecipientName()
            ));
        }
        rows.sort(Comparator.comparing(ReportRow::date).thenComparing(ReportRow::orderNumber));
        return rows;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String money(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String sanitize(String text) {
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private record ReportRow(LocalDate date, String orderNumber, String status, BigDecimal totalAmount, int itemCount, String recipientName) {
    }
}
