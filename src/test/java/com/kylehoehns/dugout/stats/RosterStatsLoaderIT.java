package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RosterStatsLoaderIT {

	@Autowired
	RosterStatsLoader rosterStatsLoader;

	@Autowired
	PlayerStatsRepository playerStatsRepository;

	@Test
	@DisplayName("seeds the roster stats table from the CSV file when the table is empty")
	void should_seed_roster_stats_from_csv_when_table_is_empty() throws Exception {
		// given
		playerStatsRepository.deleteAll();

		// when
		rosterStatsLoader.run();
		long count = playerStatsRepository.count();
		Optional<PlayerStats> hoehns = playerStatsRepository.findById(4);
		Optional<PlayerStats> lane = playerStatsRepository.findById(92);

		// then
		assertThat(count).isEqualTo(12);
		assertThat(hoehns).isPresent();
		assertThat(hoehns.get().getFirstName()).isEqualTo("Tate");
		assertThat(hoehns.get().getLastName()).isEqualTo("Hoehns");
		assertThat(hoehns.get().getHits()).isEqualTo(12);
		assertThat(lane).isPresent();
		assertThat(lane.get().getStolenBases()).isEqualTo(41);
	}

	@Test
	@DisplayName("does not reseed the roster stats table when it already has data")
	void should_not_reseed_roster_stats_when_table_already_has_data() throws Exception {
		// given
		playerStatsRepository.deleteAll();
		playerStatsRepository.saveAll(List.of(
			new PlayerStats(1, "Existing", "Player", 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0)
		));

		// when
		rosterStatsLoader.run();
		long count = playerStatsRepository.count();

		// then
		assertThat(count).isEqualTo(1);
		assertThat(playerStatsRepository.findById(4)).isEmpty();
	}

	@Test
	@DisplayName("throws an illegal state exception when a CSV row has fewer than 14 columns")
	void should_throw_illegal_state_exception_when_row_has_too_few_columns() {
		// given
		String malformedLine = "1,Jane,Doe";

		// when / then
		assertThatThrownBy(() -> rosterStatsLoader.parseLine(malformedLine))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("roster-stats.csv")
			.hasMessageContaining("14 columns");
	}
}
