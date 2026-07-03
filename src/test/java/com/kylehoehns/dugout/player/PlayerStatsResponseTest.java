package com.kylehoehns.dugout.player;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerStatsResponseTest {

    @Test
    @DisplayName("computes the full name and rounded batting average from a player's stats")
    void should_compute_name_and_batting_avg_when_converting_from_player_stats() {
        // given
        var stats = new PlayerStats(4, "Tate", "Hoehns", 16, 28, 12, 1, 0, 0, 10, 17, 9, 8, 20);

        // when
        var response = PlayerStatsResponse.from(stats);

        // then
        assertThat(response.jerseyNumber()).isEqualTo(4);
        assertThat(response.name()).isEqualTo("Tate Hoehns");
        assertThat(response.battingAvg()).isEqualTo(0.429);
    }

    @Test
    @DisplayName("returns a batting average of 0.000 when at-bats is null")
    void should_return_zero_batting_avg_when_at_bats_is_null() {
        // given
        var stats = new PlayerStats();
        stats.setJerseyNumber(9);
        stats.setFirstName("No");
        stats.setLastName("AtBats");
        stats.setHits(0);
        stats.setAtBats(null);

        // when
        var response = PlayerStatsResponse.from(stats);

        // then
        assertThat(response.battingAvg()).isEqualTo(0.000);
        assertThat(response.battingAvg()).isNotNaN();
    }

    @Test
    @DisplayName("returns a batting average of 0.000 when hits is null but at-bats is positive")
    void should_return_zero_batting_avg_when_hits_is_null_and_at_bats_is_positive() {
        // given
        var stats = new PlayerStats();
        stats.setJerseyNumber(11);
        stats.setFirstName("No");
        stats.setLastName("Hits");
        stats.setHits(null);
        stats.setAtBats(10);

        // when
        var response = PlayerStatsResponse.from(stats);

        // then
        assertThat(response.battingAvg()).isEqualTo(0.000);
        assertThat(response.battingAvg()).isNotNaN();
    }

    @Test
    @DisplayName("builds the name without the last name when last name is null")
    void should_build_name_when_last_name_is_null() {
        // given
        var stats = new PlayerStats();
        stats.setJerseyNumber(4);
        stats.setFirstName("Tate");
        stats.setLastName(null);

        // when
        var response = PlayerStatsResponse.from(stats);

        // then
        assertThat(response.name()).isEqualTo("Tate");
    }

    @Test
    @DisplayName("builds an empty name when both first and last name are null")
    void should_build_empty_name_when_first_and_last_name_are_null() {
        // given
        var stats = new PlayerStats();
        stats.setJerseyNumber(7);
        stats.setFirstName(null);
        stats.setLastName(null);

        // when
        var response = PlayerStatsResponse.from(stats);

        // then
        assertThat(response.name()).isEmpty();
    }
}
