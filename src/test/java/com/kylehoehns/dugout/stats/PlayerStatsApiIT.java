package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
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

    @Autowired RosterStatsLoader rosterStatsLoader;

    @BeforeEach
    void setUp() {
        playerStatsRepository.deleteAll();
    }

    @Test
    @DisplayName("returns all stats sorted by batting average descending")
    void should_return_all_stats_sorted_by_batting_average_descending_when_listing()
            throws Exception {
        // given
        playerStatsRepository.saveAll(
                List.of(
                        new PlayerStats(1, "Low", "Average", 10, 40, 8, 0, 0, 0, 5, 5, 2, 5, 3),
                        new PlayerStats(2, "High", "Average", 10, 40, 20, 0, 0, 0, 10, 10, 2, 5, 3),
                        new PlayerStats(3, "Mid", "Average", 10, 40, 14, 0, 0, 0, 8, 8, 2, 5, 3)));

        // when
        var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats).hasSize(3);
        assertThat(stats)
                .extracting(PlayerStatsResponse::name)
                .containsExactly("High Average", "Mid Average", "Low Average");
    }

    @Test
    @DisplayName("returns the player's stats when the jersey number exists")
    void should_return_player_stats_when_jersey_number_exists() throws Exception {
        // given
        playerStatsRepository.save(
                new PlayerStats(4, "Tate", "Hoehns", 16, 28, 12, 1, 0, 0, 10, 17, 9, 8, 20));

        // when
        var response = mockMvc.perform(get("/api/stats/{number}", 4)).andReturn().getResponse();
        var result =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(result.name()).isEqualTo("Tate Hoehns");
        assertThat(result.atBats()).isEqualTo(28);
        assertThat(result.hits()).isEqualTo(12);
        assertThat(result.battingAvg()).isEqualByComparingTo(new BigDecimal("0.429"));
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
    @DisplayName("returns a batting average of 0.000 when at-bats is zero")
    void should_return_zero_batting_average_when_at_bats_is_zero() throws Exception {
        // given
        playerStatsRepository.save(
                new PlayerStats(7, "Bench", "Warmer", 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));

        // when
        var response = mockMvc.perform(get("/api/stats/{number}", 7)).andReturn().getResponse();
        var result =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(result.atBats()).isEqualTo(0);
        assertThat(result.hits()).isEqualTo(0);
        assertThat(result.battingAvg()).isEqualByComparingTo(new BigDecimal("0.000"));
    }

    @Test
    @DisplayName("loads the full roster from the CSV on startup")
    void should_load_roster_from_csv_when_table_is_empty() throws Exception {
        // given
        playerStatsRepository.deleteAll();

        // when
        rosterStatsLoader.run();

        // then
        assertThat(playerStatsRepository.count()).isEqualTo(12);

        var response = mockMvc.perform(get("/api/stats/{number}", 4)).andReturn().getResponse();
        var result =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        assertThat(result.name()).isEqualTo("Tate Hoehns");
        assertThat(result.battingAvg()).isEqualByComparingTo(new BigDecimal("0.429"));

        var otherResponse =
                mockMvc.perform(get("/api/stats/{number}", 92)).andReturn().getResponse();
        var otherResult =
                objectMapper.readValue(
                        otherResponse.getContentAsString(), PlayerStatsResponse.class);
        assertThat(otherResult.stolenBases()).isEqualTo(41);
    }
}
