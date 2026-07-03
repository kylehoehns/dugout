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
    double battingAvg
) {

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
            calculateBattingAvg(stats.getHits(), stats.getAtBats())
        );
    }

    private static double calculateBattingAvg(Integer hits, Integer atBats) {
        if (atBats == null || atBats == 0 || hits == null) {
            return 0.000;
        }
        return BigDecimal.valueOf(hits)
            .divide(BigDecimal.valueOf(atBats), 3, RoundingMode.HALF_UP)
            .doubleValue();
    }
}
