package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerStatsTest {

    @Test
    @DisplayName("exposes every field via its getter after being set on a no-arg instance")
    void should_expose_every_field_when_set_on_no_arg_instance() {
        // given
        var stats = new PlayerStats();

        // when
        stats.setJerseyNumber(9);
        stats.setFirstName("Ted");
        stats.setLastName("Williams");
        stats.setGamesPlayed(30);
        stats.setAtBats(90);
        stats.setHits(40);
        stats.setDoubles(8);
        stats.setTriples(2);
        stats.setHomeRuns(10);
        stats.setRbi(35);
        stats.setRuns(25);
        stats.setWalks(15);
        stats.setStrikeouts(12);
        stats.setStolenBases(3);

        // then
        assertThat(stats.getJerseyNumber()).isEqualTo(9);
        assertThat(stats.getFirstName()).isEqualTo("Ted");
        assertThat(stats.getLastName()).isEqualTo("Williams");
        assertThat(stats.getGamesPlayed()).isEqualTo(30);
        assertThat(stats.getAtBats()).isEqualTo(90);
        assertThat(stats.getHits()).isEqualTo(40);
        assertThat(stats.getDoubles()).isEqualTo(8);
        assertThat(stats.getTriples()).isEqualTo(2);
        assertThat(stats.getHomeRuns()).isEqualTo(10);
        assertThat(stats.getRbi()).isEqualTo(35);
        assertThat(stats.getRuns()).isEqualTo(25);
        assertThat(stats.getWalks()).isEqualTo(15);
        assertThat(stats.getStrikeouts()).isEqualTo(12);
        assertThat(stats.getStolenBases()).isEqualTo(3);
    }
}
