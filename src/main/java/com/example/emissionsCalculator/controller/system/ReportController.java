package com.example.emissionsCalculator.controller.system;

import com.example.emissionsCalculator.service.system.ReportExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportExportService reportExportService;

    @GetMapping("/scientific-results/csv")
    public void exportScientificCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_scientific_results.csv");

        reportExportService.writeScientificResultsCsv(response.getWriter());
    }
}