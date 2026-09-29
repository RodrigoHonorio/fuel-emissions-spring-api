package uk.org.spire.emissionsCalculator.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpireConstantsTest {

    @Test
    @DisplayName("Should accept only coordinates inside the Greater London bounding box")
    void shouldValidateLondonBounds() {
        assertTrue(SpireConstants.isWithinLondon(51.5074, -0.1278));
        assertTrue(SpireConstants.isWithinLondon(51.28, -0.51));
        assertFalse(SpireConstants.isWithinLondon(51.27, -0.1278));
        assertFalse(SpireConstants.isWithinLondon(51.5074, 0.34));
        assertFalse(SpireConstants.isWithinLondon(55.9533, -3.1883));
    }
}
