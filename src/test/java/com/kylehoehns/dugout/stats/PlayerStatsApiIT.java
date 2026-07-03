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
			new PlayerStats(1, "Micah", "Flynn", 14, 21, 7, 0, 0, 0, 4, 10, 6, 9, 8)
		));
	}

	@Test
	@DisplayName("returns all players sorted by battingAvg descending when listing")
	void should_return_all_players_sorted_by_batting_avg_descending_when_listing() throws Exception {
		// given
		seedRoster();

		// when
		var response = mockMvc.perform(get("/api/stats"))
			.andReturn().getResponse();
		var stats = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(stats).hasSize(3);
		// #92 26/45 = 0.578, #4 12/28 = 0.429, #1 7/21 = 0.333
		assertThat(stats[0].jerseyNumber()).isEqualTo(92);
		assertThat(stats[1].jerseyNumber()).isEqualTo(4);
		assertThat(stats[2].jerseyNumber()).isEqualTo(1);
		assertThat(stats)
			.extracting(PlayerStatsResponse::battingAvg)
			.isSortedAccordingTo((a, b) -> Double.compare(b, a));
	}

	@Test
	@DisplayName("returns the player stats with computed battingAvg when the jersey number exists")
	void should_return_player_stats_when_jersey_number_exists() throws Exception {
		// given
		seedRoster();

		// when
		var response = mockMvc.perform(get("/api/stats/{number}", 4))
			.andReturn().getResponse();
		var result = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(result.jerseyNumber()).isEqualTo(4);
		assertThat(result.name()).isEqualTo("Tate Hoehns");
		assertThat(result.atBats()).isEqualTo(28);
		assertThat(result.hits()).isEqualTo(12);
		assertThat(result.battingAvg()).isEqualTo(0.429);
	}

	@Test
	@DisplayName("returns stolenBases for a player when the jersey number exists")
	void should_return_stolen_bases_when_jersey_number_exists() throws Exception {
		// given
		seedRoster();

		// when
		var response = mockMvc.perform(get("/api/stats/{number}", 92))
			.andReturn().getResponse();
		var result = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(result.stolenBases()).isEqualTo(41);
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
	@DisplayName("returns a battingAvg of 0.000 when the player has zero at-bats")
	void should_return_zero_batting_avg_when_at_bats_is_zero() throws Exception {
		// given
		playerStatsRepository.save(
			new PlayerStats(50, "Bench", "Warmer", 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
		);

		// when
		var response = mockMvc.perform(get("/api/stats/{number}", 50))
			.andReturn().getResponse();
		var result = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(result.atBats()).isEqualTo(0);
		assertThat(result.battingAvg()).isEqualTo(0.000);
	}

	@Test
	@DisplayName("returns a battingAvg of 0.000 when the player has a null hits value")
	void should_return_zero_batting_avg_when_hits_is_null() throws Exception {
		// given
		playerStatsRepository.save(
			new PlayerStats(51, "No", "Hits", 5, 10, null, 0, 0, 0, 0, 0, 0, 0, 0)
		);

		// when
		var response = mockMvc.perform(get("/api/stats/{number}", 51))
			.andReturn().getResponse();
		var result = objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(result.hits()).isNull();
		assertThat(result.battingAvg()).isEqualTo(0.000);
	}
}
