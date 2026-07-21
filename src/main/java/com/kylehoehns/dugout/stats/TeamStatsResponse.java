package com.kylehoehns.dugout.stats;

import java.math.BigDecimal;
import java.util.List;

public record TeamStatsResponse(BigDecimal teamBattingAvg, List<PlayerStatsResponse> players) {

    public static TeamStatsResponse from(List<PlayerStatsResponse> players) {
        int totalHits = players.stream().mapToInt(p -> p.hits() == null ? 0 : p.hits()).sum();
        int totalAtBats = players.stream().mapToInt(p -> p.atBats() == null ? 0 : p.atBats()).sum();
        return new TeamStatsResponse(BattingAverage.of(totalHits, totalAtBats), players);
    }
}
