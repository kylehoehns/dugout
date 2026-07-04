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

    @Autowired MockMvc mockMvc;

    @Autowired ObjectMapper objectMapper;

    @Autowired PlayerStatsRepository playerStatsRepository;

    @BeforeEach
    void setUp() {
        playerStatsRepository.deleteAll();
    }

    @Test
    @DisplayName("returns stats with computed batting average when the jersey number exists")
    void should_return_stats_when_jersey_number_exists() throws Exception {
        // given
        playerStatsRepository.save(
                new PlayerStats(4, "Tate", "Hoehns", 20, 28, 12, 3, 1, 2, 10, 15, 5, 6, 8));

        // when
        var response = mockMvc.perform(get("/api/stats/{number}", 4)).andReturn().getResponse();
        var result =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(result.name()).isEqualTo("Tate Hoehns");
        assertThat(result.atBats()).isEqualTo(28);
        assertThat(result.hits()).isEqualTo(12);
        assertThat(result.battingAvg()).isEqualTo(0.429);
    }

    @Test
    @DisplayName("returns stolen bases for the requested jersey number")
    void should_return_stolen_bases_when_jersey_number_exists() throws Exception {
        // given
        playerStatsRepository.save(
                new PlayerStats(92, "Cooper", "Lane", 22, 60, 20, 4, 0, 1, 12, 30, 10, 9, 41));

        // when
        var response = mockMvc.perform(get("/api/stats/{number}", 92)).andReturn().getResponse();
        var result =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

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
        var status =
                mockMvc.perform(get("/api/stats/{number}", unknownNumber))
                        .andReturn()
                        .getResponse()
                        .getStatus();

        // then
        assertThat(status).isEqualTo(404);
    }

    @Test
    @DisplayName("returns all stats sorted by batting average descending")
    void should_return_all_stats_sorted_by_batting_average_descending() throws Exception {
        // given
        playerStatsRepository.saveAll(
                List.of(
                        new PlayerStats(1, "Low", "Average", 20, 100, 20, 2, 0, 0, 5, 10, 5, 10, 2),
                        new PlayerStats(
                                2, "High", "Average", 20, 100, 50, 5, 1, 2, 20, 25, 8, 5, 4),
                        new PlayerStats(
                                3, "Mid", "Average", 20, 100, 35, 3, 0, 1, 15, 18, 6, 8, 3)));

        // when
        var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var results =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(results).hasSize(3);
        assertThat(results[0].name()).isEqualTo("High Average");
        assertThat(results)
                .extracting(PlayerStatsResponse::battingAvg)
                .isSortedAccordingTo((a, b) -> Double.compare(b, a));
    }

    @Test
    @DisplayName("returns 0.000 batting average when at-bats is zero")
    void should_return_zero_batting_average_when_at_bats_is_zero() throws Exception {
        // given
        playerStatsRepository.save(
                new PlayerStats(50, "Bench", "Warmer", 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));

        // when
        var response = mockMvc.perform(get("/api/stats/{number}", 50)).andReturn().getResponse();
        var result =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(result.battingAvg()).isEqualTo(0.000);
        assertThat(Double.isNaN(result.battingAvg())).isFalse();
        assertThat(Double.isInfinite(result.battingAvg())).isFalse();
    }

    @Test
    @DisplayName("returns 0.000 batting average when hits is null and at-bats is positive")
    void should_return_zero_batting_average_when_hits_is_null() throws Exception {
        // given
        var stats = new PlayerStats(61, "No", "Hits", 5, 10, null, 0, 0, 0, 0, 0, 0, 0, 0);
        playerStatsRepository.save(stats);

        // when
        var response = mockMvc.perform(get("/api/stats/{number}", 61)).andReturn().getResponse();
        var result =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(result.hits()).isNull();
        assertThat(result.battingAvg()).isEqualTo(0.000);
    }

    @Test
    @DisplayName("returns an empty list when no stats are seeded")
    void should_return_empty_list_when_no_stats_are_seeded() throws Exception {
        // given
        // repository already cleared in setUp

        // when
        var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var results =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(results).isEmpty();
    }
}
