package com.kylehoehns.dugout.stats;

import java.math.BigDecimal;
import java.util.List;

public record TeamSummaryResponse(BigDecimal teamBattingAvg, List<PlayerStatsResponse> players) {}
