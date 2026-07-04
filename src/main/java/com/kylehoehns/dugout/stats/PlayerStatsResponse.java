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
        double battingAvg) {

    public static PlayerStatsResponse from(PlayerStats stats) {
        double battingAvg =
                stats.getAtBats() == null || stats.getAtBats() == 0
                        ? 0.000
                        : BigDecimal.valueOf(stats.getHits() == null ? 0 : stats.getHits())
                                .divide(
                                        BigDecimal.valueOf(stats.getAtBats()),
                                        3,
                                        RoundingMode.HALF_UP)
                                .doubleValue();

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
                battingAvg);
    }
}
