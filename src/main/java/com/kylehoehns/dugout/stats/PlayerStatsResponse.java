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
                computeBattingAvg(stats.getHits(), stats.getAtBats()));
    }

    private static BigDecimal computeBattingAvg(Integer hits, Integer atBats) {
        if (atBats == null || atBats == 0) {
            return BigDecimal.ZERO.setScale(3);
        }
        return BigDecimal.valueOf(hits == null ? 0 : hits)
                .divide(BigDecimal.valueOf(atBats), 3, RoundingMode.HALF_UP);
    }
}
