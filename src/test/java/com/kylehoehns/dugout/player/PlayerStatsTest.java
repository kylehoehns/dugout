package com.kylehoehns.dugout.player;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerStatsTest {

    @Test
    @DisplayName("exposes every field via its getter and setter")
    void should_expose_every_field_when_using_getters_and_setters() {
        // given
        var stats = new PlayerStats();

        // when
        stats.setJerseyNumber(4);
        stats.setFirstName("Tate");
        stats.setLastName("Hoehns");
        stats.setGamesPlayed(16);
        stats.setAtBats(28);
        stats.setHits(12);
        stats.setDoubles(1);
        stats.setTriples(0);
        stats.setHomeRuns(0);
        stats.setRbi(10);
        stats.setRuns(17);
        stats.setWalks(9);
        stats.setStrikeouts(8);
        stats.setStolenBases(20);

        // then
        assertThat(stats.getJerseyNumber()).isEqualTo(4);
        assertThat(stats.getFirstName()).isEqualTo("Tate");
        assertThat(stats.getLastName()).isEqualTo("Hoehns");
        assertThat(stats.getGamesPlayed()).isEqualTo(16);
        assertThat(stats.getAtBats()).isEqualTo(28);
        assertThat(stats.getHits()).isEqualTo(12);
        assertThat(stats.getDoubles()).isEqualTo(1);
        assertThat(stats.getTriples()).isEqualTo(0);
        assertThat(stats.getHomeRuns()).isEqualTo(0);
        assertThat(stats.getRbi()).isEqualTo(10);
        assertThat(stats.getRuns()).isEqualTo(17);
        assertThat(stats.getWalks()).isEqualTo(9);
        assertThat(stats.getStrikeouts()).isEqualTo(8);
        assertThat(stats.getStolenBases()).isEqualTo(20);
    }

    @Test
    @DisplayName("populates every field when using the all-args constructor")
    void should_populate_every_field_when_using_all_args_constructor() {
        // given
        var stats = new PlayerStats(92, "Cooper", "Lane", 23, 45, 26, 2, 0, 0, 4, 37, 18, 9, 41);

        // then
        assertThat(stats.getJerseyNumber()).isEqualTo(92);
        assertThat(stats.getFirstName()).isEqualTo("Cooper");
        assertThat(stats.getLastName()).isEqualTo("Lane");
        assertThat(stats.getGamesPlayed()).isEqualTo(23);
        assertThat(stats.getAtBats()).isEqualTo(45);
        assertThat(stats.getHits()).isEqualTo(26);
        assertThat(stats.getDoubles()).isEqualTo(2);
        assertThat(stats.getTriples()).isEqualTo(0);
        assertThat(stats.getHomeRuns()).isEqualTo(0);
        assertThat(stats.getRbi()).isEqualTo(4);
        assertThat(stats.getRuns()).isEqualTo(37);
        assertThat(stats.getWalks()).isEqualTo(18);
        assertThat(stats.getStrikeouts()).isEqualTo(9);
        assertThat(stats.getStolenBases()).isEqualTo(41);
    }
}
