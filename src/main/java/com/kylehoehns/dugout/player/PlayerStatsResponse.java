package com.kylehoehns.dugout.player;

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
        String firstName = stats.getFirstName() == null ? "" : stats.getFirstName();
        String lastName = stats.getLastName() == null ? "" : stats.getLastName();
        String name = (firstName + " " + lastName).trim();
        return new PlayerStatsResponse(
                stats.getJerseyNumber(),
                name,
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
                calculateBattingAvg(stats.getHits(), stats.getAtBats()));
    }

    private static double calculateBattingAvg(Integer hits, Integer atBats) {
        if (atBats == null || atBats == 0) {
            return 0.000;
        }
        int safeHits = hits == null ? 0 : hits;
        return BigDecimal.valueOf(safeHits)
                .divide(BigDecimal.valueOf(atBats), 3, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
