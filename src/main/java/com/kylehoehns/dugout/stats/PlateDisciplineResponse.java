package com.kylehoehns.dugout.stats;

import java.math.BigDecimal;

public record PlateDisciplineResponse(
        Integer jerseyNumber, String name, Integer walks, Integer strikeouts, BigDecimal eye) {

    public static PlateDisciplineResponse from(PlayerStatsResponse playerStats) {
        Integer walks = playerStats.walks();
        Integer strikeouts = playerStats.strikeouts();
        int safeStrikeouts = strikeouts == null ? 0 : strikeouts;
        int effectiveStrikeouts = safeStrikeouts == 0 ? 1 : safeStrikeouts;
        BigDecimal eye = PlayerStatsResponse.roundedQuotient(walks, effectiveStrikeouts);
        return new PlateDisciplineResponse(
                playerStats.jerseyNumber(), playerStats.name(), walks, strikeouts, eye);
    }
}
