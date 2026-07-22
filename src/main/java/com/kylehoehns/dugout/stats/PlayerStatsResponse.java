package com.kylehoehns.dugout.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PlayerStatsResponse(
        Integer jerseyNumber,
        String name,
        Integer gamesPlayed,
        Integer atBats,
        Integer hits,
        Integer doubles,
        Integer triples,
        Integer homeRuns,
        Integer rbi,
        Integer runs,
        Integer walks,
        Integer strikeouts,
        Integer stolenBases,
        BigDecimal battingAvg) {

    static final int BATTING_AVG_SCALE = 3;
    static final RoundingMode BATTING_AVG_ROUNDING = RoundingMode.HALF_UP;

    public static PlayerStatsResponse from(PlayerStats stats) {
        return new PlayerStatsResponse(
                stats.getJerseyNumber(),
                stats.getFirstName() + " " + stats.getLastName(),
                stats.getGamesPlayed(),
                stats.getAtBats(),
                stats.getHits(),
                stats.getDoubles(),
                stats.getTriples(),
                stats.getHomeRuns(),
                stats.getRbi(),
                stats.getRuns(),
                stats.getWalks(),
                stats.getStrikeouts(),
                stats.getStolenBases(),
                battingAverage(stats.getHits(), stats.getAtBats()));
    }

    static BigDecimal battingAverage(Integer hits, Integer atBats) {
        int safeHits = hits == null ? 0 : hits;
        return roundedQuotient(safeHits, atBats);
    }

    static BigDecimal roundedQuotient(Integer numerator, Integer denominator) {
        if (denominator == null || denominator == 0) {
            return BigDecimal.ZERO.setScale(BATTING_AVG_SCALE, BATTING_AVG_ROUNDING);
        }
        int safeNumerator = numerator == null ? 0 : numerator;
        return BigDecimal.valueOf(safeNumerator)
                .divide(BigDecimal.valueOf(denominator), BATTING_AVG_SCALE, BATTING_AVG_ROUNDING);
    }
}
