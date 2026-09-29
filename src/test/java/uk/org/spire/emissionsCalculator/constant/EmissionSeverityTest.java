package uk.org.spire.emissionsCalculator.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmissionSeverityTest {

    @Test
    @DisplayName("Should classify the daily VOC emission into the expected severity band")
    void shouldClassifyDailyVocEmission() {
        assertEquals(EmissionSeverity.LOW, EmissionSeverity.fromDailyVocKg(0.0));
        assertEquals(EmissionSeverity.LOW, EmissionSeverity.fromDailyVocKg(50.0));
        assertEquals(EmissionSeverity.MODERATE, EmissionSeverity.fromDailyVocKg(50.01));
        assertEquals(EmissionSeverity.MODERATE, EmissionSeverity.fromDailyVocKg(150.0));
        assertEquals(EmissionSeverity.HIGH, EmissionSeverity.fromDailyVocKg(150.01));
        assertEquals(EmissionSeverity.HIGH, EmissionSeverity.fromDailyVocKg(300.0));
        assertEquals(EmissionSeverity.CRITICAL, EmissionSeverity.fromDailyVocKg(300.01));
    }

    @Test
    @DisplayName("Should expose the rendering colour of each severity band")
    void shouldExposeMarkerColour() {
        assertEquals("#28a745", EmissionSeverity.LOW.getMarkerColour());
        assertEquals("#ffc107", EmissionSeverity.MODERATE.getMarkerColour());
        assertEquals("#fd7e14", EmissionSeverity.HIGH.getMarkerColour());
        assertEquals("#dc3545", EmissionSeverity.CRITICAL.getMarkerColour());
    }
}
