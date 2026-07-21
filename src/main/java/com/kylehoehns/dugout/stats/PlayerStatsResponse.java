package com.kylehoehns.dugout.stats;

import java.math.BigDecimal;

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

    private static BigDecimal battingAverage(Integer hits, Integer atBats) {
        int safeHits = hits == null ? 0 : hits;
        long safeAtBats = atBats == null ? 0 : atBats;
        return BattingAverage.of(safeHits, safeAtBats);
    }
}
