package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerStatsResponseTest {

    @Test
    @DisplayName("treats null hits as zero when computing batting average")
    void should_treat_null_hits_as_zero_when_computing_batting_average() {
        // given
        var stats = new PlayerStats(7, "Null", "Hitter", 10, 20, null, 0, 0, 0, 0, 0, 0, 0, 0);

        // when
        var response = PlayerStatsResponse.from(stats);

        // then
        assertThat(response.hits()).isNull();
        assertThat(response.battingAvg()).isEqualByComparingTo(new BigDecimal("0.000"));
    }

    @Test
    @DisplayName("returns a batting average of 0.000 when at-bats is null")
    void should_return_zero_batting_average_when_at_bats_is_null() {
        // given
        var stats = new PlayerStats(8, "Null", "AtBats", 10, null, 5, 0, 0, 0, 0, 0, 0, 0, 0);

        // when
        var response = PlayerStatsResponse.from(stats);

        // then
        assertThat(response.battingAvg()).isEqualByComparingTo(new BigDecimal("0.000"));
    }

    @Test
    @DisplayName("computes batting average when hits and at-bats are present")
    void should_compute_batting_average_when_hits_and_at_bats_are_present() {
        // given
        var stats = new PlayerStats(9, "Solid", "Hitter", 10, 20, 5, 0, 0, 0, 0, 0, 0, 0, 0);

        // when
        var response = PlayerStatsResponse.from(stats);

        // then
        assertThat(response.battingAvg()).isEqualByComparingTo(new BigDecimal("0.250"));
    }
}
