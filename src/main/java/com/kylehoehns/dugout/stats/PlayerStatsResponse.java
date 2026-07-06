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

    public static PlayerStatsResponse from(PlayerStats playerStats) {
        String name = playerStats.getFirstName() + " " + playerStats.getLastName();
        return new PlayerStatsResponse(
                playerStats.getJerseyNumber(),
                name,
                playerStats.getGamesPlayed(),
                playerStats.getAtBats(),
                playerStats.getHits(),
                playerStats.getDoubles(),
                playerStats.getTriples(),
                playerStats.getHomeRuns(),
                playerStats.getRbi(),
                playerStats.getRuns(),
                playerStats.getWalks(),
                playerStats.getStrikeouts(),
                playerStats.getStolenBases(),
                battingAverage(playerStats.getHits(), playerStats.getAtBats()));
    }

    private static double battingAverage(Integer hits, Integer atBats) {
        if (atBats == null || atBats == 0) {
            return 0.000;
        }
        int hitCount = hits == null ? 0 : hits;
        return BigDecimal.valueOf(hitCount)
                .divide(BigDecimal.valueOf(atBats), 10, RoundingMode.HALF_UP)
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
