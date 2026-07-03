package com.kylehoehns.dugout.stats;

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
        String name = stats.getFirstName() + " " + stats.getLastName();
        double battingAvg = calculateBattingAvg(stats.getHits(), stats.getAtBats());
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
            battingAvg
        );
    }

    private static double calculateBattingAvg(Integer hits, Integer atBats) {
        if (atBats == null || atBats == 0 || hits == null) {
            return 0.0;
        }
        return Math.round((hits / (double) atBats) * 1000) / 1000.0;
    }
}
