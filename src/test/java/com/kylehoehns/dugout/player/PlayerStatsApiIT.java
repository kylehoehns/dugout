package com.kylehoehns.dugout.player;

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

    private void seedRoster() {
        playerStatsRepository.saveAll(
                List.of(
                        new PlayerStats(
                                23, "Mason", "Reed", 23, 41, 25, 10, 0, 0, 20, 25, 12, 1, 13),
                        new PlayerStats(
                                92, "Cooper", "Lane", 23, 45, 26, 2, 0, 0, 4, 37, 18, 9, 41),
                        new PlayerStats(
                                4, "Tate", "Hoehns", 16, 28, 12, 1, 0, 0, 10, 17, 9, 8, 20)));
    }

    @Test
    @DisplayName("returns all stats sorted by batting average descending when listing")
    void should_return_all_stats_sorted_by_batting_average_descending_when_listing()
            throws Exception {
        // given
        seedRoster();

        // when
        var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats).hasSize(3);
        assertThat(stats[0].battingAvg())
                .isGreaterThanOrEqualTo(stats[1].battingAvg())
                .isGreaterThanOrEqualTo(stats[2].battingAvg());
        assertThat(stats[0].name()).isEqualTo("Mason Reed");
    }

    @Test
    @DisplayName("returns the player's stats when the jersey number exists")
    void should_return_player_stats_when_jersey_number_exists() throws Exception {
        // given
        seedRoster();

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
    @DisplayName("returns the stolen bases when querying jersey number 92")
    void should_return_stolen_bases_when_querying_jersey_92() throws Exception {
        // given
        seedRoster();

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
        seedRoster();

        // when
        var status =
                mockMvc.perform(get("/api/stats/{number}", 777))
                        .andReturn()
                        .getResponse()
                        .getStatus();

        // then
        assertThat(status).isEqualTo(404);
    }

    @Test
    @DisplayName("returns a zero batting average when the player has zero at-bats")
    void should_return_zero_batting_average_when_player_has_zero_at_bats() throws Exception {
        // given
        playerStatsRepository.save(
                new PlayerStats(7, "Bench", "Warmer", 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));

        // when
        var response = mockMvc.perform(get("/api/stats/{number}", 7)).andReturn().getResponse();
        var result =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(result.atBats()).isEqualTo(0);
        assertThat(result.battingAvg()).isEqualTo(0.000);
    }
}
