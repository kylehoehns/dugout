package com.kylehoehns.dugout.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class BattingAverage {
    static final int SCALE = 3;
    static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private BattingAverage() {}

    static BigDecimal of(long hits, long atBats) {
        if (atBats == 0) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }
        return BigDecimal.valueOf(hits).divide(BigDecimal.valueOf(atBats), SCALE, ROUNDING);
    }
}
