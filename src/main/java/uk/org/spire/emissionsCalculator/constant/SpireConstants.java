package uk.org.spire.emissionsCalculator.constant;

/**
 * Global constants utilised across the S.P.I.R.E. ecosystem.
 * <p>
 * This class cannot be instantiated. It serves solely as a centralised
 * repository for static, immutable configuration values.
 * </p>
 */
public final class SpireConstants {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SpireConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * The standard Spatial Reference System Identifier (SRID) for WGS 84.
     * Utilised globally for GPS tracking and standard mapping systems.
     */
    public static final int WGS84_SRID = 4326;

}