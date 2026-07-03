package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerStatsTest {

    @Test
    @DisplayName("exposes every field through its getters and setters")
    void should_expose_all_fields_when_using_getters_and_setters() {
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
}
