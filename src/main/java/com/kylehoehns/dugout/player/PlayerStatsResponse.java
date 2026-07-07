package com.kylehoehns.dugout.player;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PlayerStatsResponse(
        Integer jerseyNumber,
        String name,
        int gamesPlayed,
        int atBats,
        int hits,
        int doubles,
        int triples,
        int homeRuns,
        int rbi,
        int runs,
        int walks,
        int strikeouts,
        int stolenBases,
        double battingAvg) {

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
                battingAvg(stats.getHits(), stats.getAtBats()));
    }

    private static double battingAvg(int hits, int atBats) {
        if (atBats == 0) {
            return 0.000;
        }
        return BigDecimal.valueOf(hits)
                .divide(BigDecimal.valueOf(atBats), 3, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
