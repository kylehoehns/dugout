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

	@Test
	@DisplayName("returns all stats sorted by batting average descending when listing")
	void should_return_all_stats_sorted_by_batting_avg_descending_when_listing() throws Exception {
		// given
		playerStatsRepository.saveAll(List.of(
			new PlayerStats(1, "Low", "Average", 20, 40, 8, 0, 0, 0, 4, 10, 6, 9, 8),
			new PlayerStats(2, "High", "Average", 20, 40, 30, 0, 0, 0, 20, 25, 12, 1, 13),
			new PlayerStats(3, "Mid", "Average", 20, 40, 16, 0, 0, 0, 10, 17, 9, 8, 20)
		));

		// when
		var response = mockMvc.perform(get("/api/stats"))
			.andReturn().getResponse();
		var stats = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(stats).hasSize(3);
		assertThat(stats[0].name()).isEqualTo("High Average");
		assertThat(stats).extracting(PlayerStatsResponse::battingAvg)
			.isSortedAccordingTo((a, b) -> Double.compare(b, a));
	}

	@Test
	@DisplayName("returns the matching player's stats when the jersey number exists")
	void should_return_matching_stats_when_jersey_number_exists() throws Exception {
		// given
		playerStatsRepository.save(
			new PlayerStats(4, "Tate", "Hoehns", 16, 28, 12, 1, 0, 0, 10, 17, 9, 8, 20));

		// when
		var response = mockMvc.perform(get("/api/stats/{number}", 4))
			.andReturn().getResponse();
		var stats = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(stats.name()).isEqualTo("Tate Hoehns");
		assertThat(stats.atBats()).isEqualTo(28);
		assertThat(stats.hits()).isEqualTo(12);
		assertThat(stats.battingAvg()).isEqualTo(0.429);
	}

	@Test
	@DisplayName("returns 404 when the jersey number is unknown")
	void should_return_404_when_jersey_number_is_unknown() throws Exception {
		// given
		int unknownNumber = 777;

		// when
		var status = mockMvc.perform(get("/api/stats/{number}", unknownNumber))
			.andReturn().getResponse().getStatus();

		// then
		assertThat(status).isEqualTo(404);
	}

	@Test
	@DisplayName("returns a batting average of 0.000 when the player has 0 at-bats")
	void should_return_zero_batting_avg_when_player_has_zero_at_bats() throws Exception {
		// given
		playerStatsRepository.save(
			new PlayerStats(7, "Bench", "Warmer", 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));

		// when
		var response = mockMvc.perform(get("/api/stats/{number}", 7))
			.andReturn().getResponse();
		var stats = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(stats.battingAvg()).isEqualTo(0.0);
		assertThat(stats.battingAvg()).isNotNaN();
		assertThat(stats.battingAvg()).isFinite();
	}
}
