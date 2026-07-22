package com.kylehoehns.dugout.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PlateDisciplineResponse(
        Integer jerseyNumber, String name, Integer walks, Integer strikeouts, BigDecimal eye) {

    private static final int EYE_SCALE = 3;
    private static final RoundingMode EYE_ROUNDING = RoundingMode.HALF_UP;

    public static PlateDisciplineResponse from(PlayerStats stats) {
        return new PlateDisciplineResponse(
                stats.getJerseyNumber(),
                stats.getFirstName() + " " + stats.getLastName(),
                stats.getWalks(),
                stats.getStrikeouts(),
                calculateEye(stats.getWalks(), stats.getStrikeouts()));
    }

    static BigDecimal calculateEye(Integer walks, Integer strikeouts) {
        int safeWalks = walks == null ? 0 : walks;
        int safeStrikeouts = strikeouts == null ? 0 : strikeouts;
        int denominator = Math.max(safeStrikeouts, 1);
        return BigDecimal.valueOf(safeWalks)
                .divide(BigDecimal.valueOf(denominator), EYE_SCALE, EYE_ROUNDING);
    }

    static int plateAppearances(Integer atBats, Integer walks) {
        int safeAtBats = atBats == null ? 0 : atBats;
        int safeWalks = walks == null ? 0 : walks;
        return safeAtBats + safeWalks;
    }
}
