package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Verifies that {@link RosterStatsLoader} seeds the roster-stats.csv contents into a fresh,
 * empty database on startup. Runs against a dedicated application context (forced fresh via
 * {@code @DirtiesContext}) so it is not affected by other tests clearing the repository.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class RosterStatsLoaderIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	PlayerStatsRepository playerStatsRepository;

	@Autowired
	RosterStatsLoader rosterStatsLoader;

	@Test
	@DisplayName("seeds the roster from the CSV file on startup when the table is empty")
	void should_seed_roster_from_csv_when_table_is_empty() throws Exception {
		// given: a fresh application context has just started (no @BeforeEach deleteAll)

		// when
		var response = mockMvc.perform(get("/api/stats"))
			.andReturn().getResponse();
		var stats = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(stats).hasSize(12);
		assertThat(playerStatsRepository.count()).isEqualTo(12);

		Optional<PlayerStats> tateHoehns = playerStatsRepository.findById(4);
		assertThat(tateHoehns).isPresent();
		assertThat(tateHoehns.get().getFirstName()).isEqualTo("Tate");
		assertThat(tateHoehns.get().getLastName()).isEqualTo("Hoehns");
		assertThat(tateHoehns.get().getAtBats()).isEqualTo(28);
		assertThat(tateHoehns.get().getHits()).isEqualTo(12);

		Optional<PlayerStats> cooperLane = playerStatsRepository.findById(92);
		assertThat(cooperLane).isPresent();
		assertThat(cooperLane.get().getStolenBases()).isEqualTo(41);
	}

	@Test
	@DisplayName("does not seed the roster again when the table is already populated")
	void should_not_reseed_roster_when_table_already_populated() throws Exception {
		// given: startup already seeded 12 rows into this fresh context (see prior test)

		// when: the loader runs again against the already-populated table
		rosterStatsLoader.run();

		// then: still exactly the CSV row count, no duplicates from a second run
		assertThat(playerStatsRepository.count()).isEqualTo(12);
	}
}
