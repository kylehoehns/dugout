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

    private void seedRoster() {
        playerStatsRepository.saveAll(
                List.of(
                        new PlayerStats(
                                23, "Mason", "Reed", 23, 41, 25, 10, 0, 0, 20, 25, 12, 1, 13),
                        new PlayerStats(
                                92, "Cooper", "Lane", 23, 45, 26, 2, 0, 0, 4, 37, 18, 9, 41),
                        new PlayerStats(36, "Owen", "Bell", 21, 27, 14, 0, 0, 0, 8, 17, 7, 5, 22),
                        new PlayerStats(
                                64, "Landon", "Cross", 23, 43, 21, 3, 0, 0, 18, 27, 13, 10, 26),
                        new PlayerStats(
                                86, "Easton", "Gray", 23, 35, 17, 0, 1, 0, 11, 27, 18, 11, 29),
                        new PlayerStats(
                                11, "Carter", "Hale", 23, 51, 23, 6, 1, 0, 22, 27, 12, 11, 20),
                        new PlayerStats(4, "Tate", "Hoehns", 16, 28, 12, 1, 0, 0, 10, 17, 9, 8, 20),
                        new PlayerStats(
                                16, "Brody", "Vance", 20, 38, 14, 4, 0, 0, 13, 18, 9, 18, 16),
                        new PlayerStats(
                                99, "Nolan", "Pierce", 23, 43, 15, 1, 0, 0, 22, 19, 12, 12, 17),
                        new PlayerStats(1, "Micah", "Flynn", 14, 21, 7, 0, 0, 0, 4, 10, 6, 9, 8),
                        new PlayerStats(45, "Jonah", "West", 23, 29, 8, 1, 0, 0, 5, 20, 22, 19, 17),
                        new PlayerStats(12, "Silas", "Fox", 22, 35, 9, 1, 0, 0, 3, 14, 11, 21, 8)));
    }

    @Test
    @DisplayName("returns all 12 roster players sorted by batting average descending")
    void should_return_all_players_sorted_by_batting_average_descending_when_listing_stats()
            throws Exception {
        // given
        seedRoster();

        // when
        var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats).hasSize(12);
        assertThat(stats[0].jerseyNumber()).isEqualTo(23);
        assertThat(stats[0].battingAvg()).isEqualTo(0.610);
        assertThat(stats)
                .extracting(PlayerStatsResponse::jerseyNumber)
                .containsExactly(23, 92, 36, 64, 86, 11, 4, 16, 99, 1, 45, 12);
    }

    @Test
    @DisplayName("returns the player stats when the jersey number exists")
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
    @DisplayName("returns the stolen bases for jersey number 92")
    void should_return_stolen_bases_when_jersey_number_is_92() throws Exception {
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
    @DisplayName("returns a batting average of 0.000 when the player has zero at-bats")
    void should_return_zero_batting_average_when_at_bats_is_zero() throws Exception {
        // given
        var zeroAtBatPlayer = new PlayerStats();
        zeroAtBatPlayer.setJerseyNumber(7);
        zeroAtBatPlayer.setFirstName("Riley");
        zeroAtBatPlayer.setLastName("Shaw");
        zeroAtBatPlayer.setGamesPlayed(5);
        zeroAtBatPlayer.setAtBats(0);
        zeroAtBatPlayer.setHits(0);
        zeroAtBatPlayer.setDoubles(0);
        zeroAtBatPlayer.setTriples(0);
        zeroAtBatPlayer.setHomeRuns(0);
        zeroAtBatPlayer.setRbi(0);
        zeroAtBatPlayer.setRuns(0);
        zeroAtBatPlayer.setWalks(0);
        zeroAtBatPlayer.setStrikeouts(0);
        zeroAtBatPlayer.setStolenBases(0);
        playerStatsRepository.save(zeroAtBatPlayer);

        // when
        var response = mockMvc.perform(get("/api/stats/{number}", 7)).andReturn().getResponse();
        var result =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(result.atBats()).isEqualTo(0);
        assertThat(result.battingAvg()).isEqualTo(0.000).isNotNaN();
    }

    @Test
    @DisplayName("returns a batting average of 0.000 when the player has null hits")
    void should_return_zero_batting_average_when_hits_is_null() throws Exception {
        // given
        var nullHitsPlayer = new PlayerStats();
        nullHitsPlayer.setJerseyNumber(8);
        nullHitsPlayer.setFirstName("Wyatt");
        nullHitsPlayer.setLastName("Sloan");
        nullHitsPlayer.setGamesPlayed(3);
        nullHitsPlayer.setAtBats(10);
        nullHitsPlayer.setHits(null);
        nullHitsPlayer.setDoubles(0);
        nullHitsPlayer.setTriples(0);
        nullHitsPlayer.setHomeRuns(0);
        nullHitsPlayer.setRbi(0);
        nullHitsPlayer.setRuns(0);
        nullHitsPlayer.setWalks(0);
        nullHitsPlayer.setStrikeouts(0);
        nullHitsPlayer.setStolenBases(0);
        playerStatsRepository.save(nullHitsPlayer);

        // when
        var response = mockMvc.perform(get("/api/stats/{number}", 8)).andReturn().getResponse();
        var result =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(result.atBats()).isEqualTo(10);
        assertThat(result.hits()).isNull();
        assertThat(result.battingAvg()).isEqualTo(0.000).isNotNaN();
    }

    @Test
    @DisplayName("breaks batting average ties by ascending jersey number when listing stats")
    void should_order_tied_batting_averages_by_jersey_number_ascending_when_listing_stats()
            throws Exception {
        // given
        playerStatsRepository.saveAll(
                List.of(
                        new PlayerStats(50, "Beau", "Ridge", 10, 20, 10, 0, 0, 0, 5, 5, 5, 5, 5),
                        new PlayerStats(10, "Grady", "Stone", 10, 10, 5, 0, 0, 0, 5, 5, 5, 5, 5)));

        // when
        var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats).extracting(PlayerStatsResponse::battingAvg).containsExactly(0.500, 0.500);
        assertThat(stats).extracting(PlayerStatsResponse::jerseyNumber).containsExactly(10, 50);
    }

    @Test
    @DisplayName("returns an empty list when there are no stats to report")
    void should_return_empty_list_when_no_stats_exist() throws Exception {
        // given (repository left empty by setUp)

        // when
        var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats).isEmpty();
    }
}
