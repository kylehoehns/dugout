package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PlayerStatsApiIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	PlayerStatsRepository playerStatsRepository;

	@BeforeEach
	void setUp() {
		playerStatsRepository.deleteAll();
	}

	private void seedRoster() {
		playerStatsRepository.saveAll(List.of(
			new PlayerStats(4, "Tate", "Hoehns", 16, 28, 12, 1, 0, 0, 10, 17, 9, 8, 20),
			new PlayerStats(92, "Cooper", "Lane", 23, 45, 26, 2, 0, 0, 4, 37, 18, 9, 41),
			new PlayerStats(36, "Owen", "Bell", 21, 27, 14, 0, 0, 0, 8, 17, 7, 5, 22)
		));
	}

	@Test
	@DisplayName("returns all players sorted by batting average descending when listing stats")
	void should_return_all_players_sorted_by_batting_avg_descending_when_listing_stats() throws Exception {
		// given
		seedRoster();

		// when
		var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
		var stats = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(stats).hasSize(3);
		assertThat(stats[0].battingAvg()).isEqualTo(0.578);
		assertThat(stats[0].name()).isEqualTo("Cooper Lane");
		assertThat(stats).isSortedAccordingTo(
			(a, b) -> Double.compare(b.battingAvg(), a.battingAvg())
		);
	}

	@Test
	@DisplayName("returns stats for Tate Hoehns when jersey number 4 is requested")
	void should_return_stats_for_tate_hoehns_when_jersey_number_4_is_requested() throws Exception {
		// given
		seedRoster();

		// when
		var response = mockMvc.perform(get("/api/stats/{number}", 4)).andReturn().getResponse();
		var result = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(result.name()).isEqualTo("Tate Hoehns");
		assertThat(result.atBats()).isEqualTo(28);
		assertThat(result.hits()).isEqualTo(12);
		assertThat(result.battingAvg()).isEqualTo(0.429);
	}

	@Test
	@DisplayName("returns 41 stolen bases when jersey number 92 is requested")
	void should_return_stolen_bases_when_jersey_number_92_is_requested() throws Exception {
		// given
		seedRoster();

		// when
		var response = mockMvc.perform(get("/api/stats/{number}", 92)).andReturn().getResponse();
		var result = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(result.name()).isEqualTo("Cooper Lane");
		assertThat(result.stolenBases()).isEqualTo(41);
	}

	@Test
	@DisplayName("returns 404 when the jersey number is unknown")
	void should_return_404_when_jersey_number_is_unknown() throws Exception {
		// given
		seedRoster();

		// when
		var status = mockMvc.perform(get("/api/stats/{number}", 777))
			.andReturn().getResponse().getStatus();

		// then
		assertThat(status).isEqualTo(404);
	}

	@Test
	@DisplayName("returns 0.000 batting average when a player has 0 at-bats")
	void should_return_zero_batting_avg_when_player_has_zero_at_bats() throws Exception {
		// given
		playerStatsRepository.save(
			new PlayerStats(7, "Bench", "Warmer", 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
		);

		// when
		var response = mockMvc.perform(get("/api/stats/{number}", 7)).andReturn().getResponse();
		var result = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(result.atBats()).isEqualTo(0);
		assertThat(result.battingAvg()).isEqualTo(0.000);
	}

	@Test
	@DisplayName("returns 0.000 batting average when a player has null hits")
	void should_return_zero_batting_avg_when_player_has_null_hits() {
		// given
		var stats = new PlayerStats(11, "No", "Hits", 3, 9, null, 0, 0, 0, 0, 0, 0, 0, 0);

		// when
		var result = PlayerStatsResponse.from(stats);

		// then
		assertThat(result.atBats()).isEqualTo(9);
		assertThat(result.hits()).isNull();
		assertThat(result.battingAvg()).isEqualTo(0.000);
	}
}
