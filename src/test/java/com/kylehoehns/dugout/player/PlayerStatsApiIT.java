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

    @Autowired RosterStatsLoader rosterStatsLoader;

    @BeforeEach
    void setUp() {
        playerStatsRepository.deleteAll();
    }

    @Test
    @DisplayName("returns all stats sorted by batting average descending when listing")
    void should_return_all_stats_sorted_by_batting_avg_descending_when_listing() throws Exception {
        // given
        playerStatsRepository.saveAll(
                List.of(
                        // 8/20 = 0.400
                        new PlayerStats(1, "Low", "Average", 10, 20, 8, 0, 0, 0, 0, 0, 0, 0, 0),
                        // 15/20 = 0.750
                        new PlayerStats(2, "High", "Average", 10, 20, 15, 0, 0, 0, 0, 0, 0, 0, 0),
                        // 10/20 = 0.500
                        new PlayerStats(3, "Mid", "Average", 10, 20, 10, 0, 0, 0, 0, 0, 0, 0, 0)));

        // when
        var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats).hasSize(3);
        assertThat(stats).extracting(PlayerStatsResponse::jerseyNumber).containsExactly(2, 3, 1);
        assertThat(stats[0].battingAvg()).isEqualTo(0.750);
        assertThat(stats[0].name()).isEqualTo("High Average");
    }

    @Test
    @DisplayName("returns the player's stats when the jersey number exists")
    void should_return_stats_when_jersey_number_exists() throws Exception {
        // given
        playerStatsRepository.save(
                new PlayerStats(4, "Tate", "Hoehns", 16, 28, 12, 1, 0, 0, 10, 17, 9, 8, 20));

        // when
        var response = mockMvc.perform(get("/api/stats/{number}", 4)).andReturn().getResponse();
        var result =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(result.jerseyNumber()).isEqualTo(4);
        assertThat(result.name()).isEqualTo("Tate Hoehns");
        assertThat(result.atBats()).isEqualTo(28);
        assertThat(result.hits()).isEqualTo(12);
        assertThat(result.battingAvg()).isEqualTo(0.429);
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
    @DisplayName("returns a batting average of 0.000 when the player has zero at-bats")
    void should_return_zero_batting_avg_when_at_bats_is_zero() throws Exception {
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
        assertThat(result.battingAvg()).isNotNaN();
    }

    @Test
    @DisplayName("loads the roster from the CSV file when the table is empty at startup")
    void should_load_roster_from_csv_when_table_is_empty_at_startup() throws Exception {
        // given
        // table already cleared in @BeforeEach, mirroring the empty-table guard

        // when
        rosterStatsLoader.run();

        // then
        var all = playerStatsRepository.findAll();
        assertThat(all).hasSize(12);

        var tate = playerStatsRepository.findById(4).orElseThrow();
        assertThat(tate.getFirstName()).isEqualTo("Tate");
        assertThat(tate.getLastName()).isEqualTo("Hoehns");
        assertThat(tate.getAtBats()).isEqualTo(28);
        assertThat(tate.getHits()).isEqualTo(12);

        var cooper = playerStatsRepository.findById(92).orElseThrow();
        assertThat(cooper.getStolenBases()).isEqualTo(41);
    }

    @Test
    @DisplayName("does not reload the roster from CSV when the table already has data")
    void should_not_reload_roster_from_csv_when_table_is_not_empty() throws Exception {
        // given
        playerStatsRepository.save(
                new PlayerStats(50, "Already", "Seeded", 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0));

        // when
        rosterStatsLoader.run();

        // then
        assertThat(playerStatsRepository.findAll()).hasSize(1);
        assertThat(playerStatsRepository.findById(50)).isPresent();
    }
}
