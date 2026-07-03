package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Exercises {@link RosterStatsLoader} directly, driving it by hand so the
 * assertions don't depend on Spring's startup ordering or on other IT classes
 * leaving the repository untouched (they don't - most clear it in @BeforeEach).
 */
@SpringBootTest
class RosterStatsLoaderIT {

	@Autowired
	RosterStatsLoader rosterStatsLoader;

	@Autowired
	PlayerStatsRepository playerStatsRepository;

	@BeforeEach
	void setUp() {
		playerStatsRepository.deleteAll();
	}

	@Test
	@DisplayName("loads the full CSV-seeded roster when the table is empty")
	void should_load_csv_seeded_roster_when_table_is_empty() throws Exception {
		// given an empty table

		// when
		rosterStatsLoader.run();

		// then
		assertThat(playerStatsRepository.count()).isEqualTo(12);
		var tate = playerStatsRepository.findById(4).orElseThrow();
		assertThat(tate.getFirstName()).isEqualTo("Tate");
		assertThat(tate.getLastName()).isEqualTo("Hoehns");
		assertThat(tate.getAtBats()).isEqualTo(28);
		assertThat(tate.getHits()).isEqualTo(12);
		var cooper = playerStatsRepository.findById(92).orElseThrow();
		assertThat(cooper.getStolenBases()).isEqualTo(41);
	}

	@Test
	@DisplayName("does not reload the roster when the table is already populated")
	void should_not_reload_roster_when_table_is_already_populated() throws Exception {
		// given a table that already has data
		playerStatsRepository.save(
			new PlayerStats(1, "Existing", "Player", 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0));

		// when
		rosterStatsLoader.run();

		// then only the pre-existing row remains, the CSV was not re-loaded
		assertThat(playerStatsRepository.count()).isEqualTo(1);
	}
}
