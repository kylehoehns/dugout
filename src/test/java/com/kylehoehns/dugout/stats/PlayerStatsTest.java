package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerStatsTest {

	@Test
	@DisplayName("stores every field when using the no-args constructor and setters")
	void should_store_every_field_when_using_no_args_constructor_and_setters() {
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
	@DisplayName("returns a zero batting average when hits is null even though at-bats is non-zero")
	void should_return_zero_batting_avg_when_hits_is_null() {
		// given
		var stats = new PlayerStats(
			4, "Tate", "Hoehns", 16, 28, null, 1, 0, 0, 10, 17, 9, 8, 20);

		// when
		var response = PlayerStatsResponse.from(stats);

		// then
		assertThat(response.battingAvg()).isEqualTo(0.0);
	}
}
