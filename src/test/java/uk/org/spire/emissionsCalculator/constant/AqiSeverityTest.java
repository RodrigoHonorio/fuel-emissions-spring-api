package uk.org.spire.emissionsCalculator.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AqiSeverityTest {

    @Test
    @DisplayName("Should map the reported air quality status into a severity band")
    void shouldMapStatusToSeverity() {
        assertEquals(AqiSeverity.HIGH, AqiSeverity.fromStatus("high"));
        assertEquals(AqiSeverity.HIGH, AqiSeverity.fromStatus(" Poor "));
        assertEquals(AqiSeverity.MODERATE, AqiSeverity.fromStatus("MODERATE"));
        assertEquals(AqiSeverity.LOW, AqiSeverity.fromStatus("low"));
        assertEquals(AqiSeverity.LOW, AqiSeverity.fromStatus("unknown"));
        assertEquals(AqiSeverity.LOW, AqiSeverity.fromStatus(null));
    }

    @Test
    @DisplayName("Should expose contrasting colours for each band")
    void shouldExposeColours() {
        assertEquals("#198754", AqiSeverity.LOW.getMarkerColour());
        assertEquals("#ffffff", AqiSeverity.LOW.getTextColour());
        assertEquals("#ffc107", AqiSeverity.MODERATE.getMarkerColour());
        assertEquals("#000000", AqiSeverity.MODERATE.getTextColour());
        assertEquals("#dc3545", AqiSeverity.HIGH.getMarkerColour());
    }
}
