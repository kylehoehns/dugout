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
    @DisplayName("returns all players sorted by battingAvg descending")
    void should_return_all_players_sorted_by_batting_avg_descending_when_listing()
            throws Exception {
        // given
        playerStatsRepository.saveAll(
                List.of(
                        new PlayerStats(4, "Tate", "Hoehns", 16, 28, 12, 1, 0, 0, 10, 17, 9, 8, 20),
                        new PlayerStats(
                                23, "Mason", "Reed", 23, 41, 25, 10, 0, 0, 20, 25, 12, 1, 13),
                        new PlayerStats(
                                45, "Jonah", "West", 23, 29, 8, 1, 0, 0, 5, 20, 22, 19, 17)));

        // when
        var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats).hasSize(3);
        assertThat(stats).extracting(PlayerStatsResponse::jerseyNumber).containsExactly(23, 4, 45);
        assertThat(stats[0].battingAvg()).isGreaterThan(stats[1].battingAvg());
        assertThat(stats[1].battingAvg()).isGreaterThan(stats[2].battingAvg());
    }

    @Test
    @DisplayName("returns the player stats when the jersey number exists")
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
        assertThat(result.atBats()).isZero();
        assertThat(result.battingAvg()).isEqualTo(0.000);
    }

    @Test
    @DisplayName("seeds the full 12-player roster from CSV when the table starts empty")
    void should_seed_full_roster_from_csv_when_table_is_empty() throws Exception {
        // given
        assertThat(playerStatsRepository.count()).isZero();
        new RosterStatsLoader(playerStatsRepository).run();

        // when
        var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats).hasSize(12);
        assertThat(stats)
                .filteredOn(stat -> stat.jerseyNumber() == 92)
                .singleElement()
                .satisfies(cooperLane -> assertThat(cooperLane.stolenBases()).isEqualTo(41));
    }

    @Test
    @DisplayName("does not reseed the roster when the table already has data")
    void should_not_reseed_roster_when_table_already_has_data() throws Exception {
        // given
        playerStatsRepository.save(
                new PlayerStats(4, "Tate", "Hoehns", 16, 28, 12, 1, 0, 0, 10, 17, 9, 8, 20));

        // when
        new RosterStatsLoader(playerStatsRepository).run();

        // then
        assertThat(playerStatsRepository.count()).isEqualTo(1);
    }
}
