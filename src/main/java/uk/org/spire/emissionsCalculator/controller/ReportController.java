package uk.org.spire.emissionsCalculator.controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generatePdfReport(
            @RequestParam String fuelType,
            @RequestParam double volume,
            @RequestParam double totalEmissionsKg,
            @RequestParam String closestStationName,
            @RequestParam double distanceKm,
            @RequestParam double latitude,
            @RequestParam double longitude) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Cabeçalho do Laudo
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD);
            Paragraph title = new Paragraph("S.P.I.R.E. - Official Environmental Impact Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC);
            Paragraph subtitle = new Paragraph("Spatial Prediction & Impact Reporting Ecosystem | London, UK", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));

            // Metadados
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD);
            document.add(new Paragraph("\n1. Simulation Metadata", sectionFont));
            document.add(new Paragraph("Generated at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            document.add(new Paragraph("Fuel Type: " + fuelType));
            document.add(new Paragraph("Fuel Volume: " + volume + " Litres"));
            document.add(new Paragraph("Target Coordinates: Lat " + latitude + ", Lng " + longitude));

            // Resultados de Impacto
            document.add(new Paragraph("\n2. Emission & Spatial Impact Analysis", sectionFont));
            document.add(new Paragraph("Total Calculated Emissions: " + String.format("%.2f", totalEmissionsKg) + " kg CO2e / Pollutants"));
            document.add(new Paragraph("Closest Monitoring Station: " + closestStationName));
            document.add(new Paragraph("Geodetic Distance to Station: " + String.format("%.2f", distanceKm) + " km"));

            // Disclaimer / NHS Context
            document.add(new Paragraph("\n3. Environmental & Public Health Compliance", sectionFont));
            document.add(new Paragraph("This report assesses local environmental stress and particulate distribution according to London air quality parameters. Ensure mitigation measures are applied if thresholds exceed local guidelines."));

            document.close();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "SPIRE_Impact_Report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(baos.toByteArray());
    }
}