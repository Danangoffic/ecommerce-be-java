package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.DashboardResponse;
import com.ecommerce.dto.response.ReportSummaryResponse;
import com.ecommerce.service.ReportExportService;
import com.ecommerce.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;

    @GetMapping("/summary")
    public ApiResponse<ReportSummaryResponse> summary() {
        return ApiResponse.success("Report fetched", reportService.getSummary());
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> dashboard(@RequestParam(required = false) Integer days) {
        return ApiResponse.success("Dashboard fetched", reportService.getDashboard(days));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "csv") String format,
                                         @RequestParam(required = false) Integer days) {
        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("xlsx".equalsIgnoreCase(format)) {
            bytes = reportExportService.exportExcel(days);
            filename = "sales-report.xlsx";
            contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else if ("pdf".equalsIgnoreCase(format)) {
            bytes = reportExportService.exportPdf(days);
            filename = "sales-report.pdf";
            contentType = MediaType.APPLICATION_PDF;
        } else {
            bytes = reportExportService.exportCsv(days);
            filename = "sales-report.csv";
            contentType = MediaType.parseMediaType("text/csv");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(contentType)
                .body(bytes);
    }
}
