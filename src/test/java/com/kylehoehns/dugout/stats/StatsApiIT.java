package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
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
class StatsApiIT {

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
    @DisplayName("returns all 12 players sorted by batting average descending when listing")
    void should_return_all_players_sorted_by_batting_avg_descending_when_listing()
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
        assertThat(stats)
                .isSortedAccordingTo((a, b) -> Double.compare(b.battingAvg(), a.battingAvg()));
        assertThat(stats[0].battingAvg())
                .isEqualTo(
                        Arrays.stream(stats)
                                .mapToDouble(PlayerStatsResponse::battingAvg)
                                .max()
                                .orElseThrow());
    }

    @Test
    @DisplayName("returns Tate Hoehns' stats when jersey number 4 exists")
    void should_return_tate_hoehns_stats_when_jersey_number_4_exists() throws Exception {
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
    @DisplayName("returns stolen bases for jersey number 92")
    void should_return_stolen_bases_when_jersey_number_92_exists() throws Exception {
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
    @DisplayName("returns all fields when a player is built via the entity setters")
    void should_return_all_fields_when_player_is_built_via_entity_setters() throws Exception {
        // given
        var stats = new PlayerStats();
        stats.setJerseyNumber(50);
        stats.setFirstName("Test");
        stats.setLastName("Player");
        stats.setGamesPlayed(10);
        stats.setAtBats(20);
        stats.setHits(5);
        stats.setDoubles(1);
        stats.setTriples(2);
        stats.setHomeRuns(3);
        stats.setRbi(6);
        stats.setRuns(7);
        stats.setWalks(4);
        stats.setStrikeouts(9);
        stats.setStolenBases(8);
        playerStatsRepository.save(stats);

        // when
        var response = mockMvc.perform(get("/api/stats/{number}", 50)).andReturn().getResponse();
        var result =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(result.jerseyNumber()).isEqualTo(50);
        assertThat(result.name()).isEqualTo("Test Player");
        assertThat(result.gamesPlayed()).isEqualTo(10);
        assertThat(result.atBats()).isEqualTo(20);
        assertThat(result.hits()).isEqualTo(5);
        assertThat(result.doubles()).isEqualTo(1);
        assertThat(result.triples()).isEqualTo(2);
        assertThat(result.homeRuns()).isEqualTo(3);
        assertThat(result.rbi()).isEqualTo(6);
        assertThat(result.runs()).isEqualTo(7);
        assertThat(result.walks()).isEqualTo(4);
        assertThat(result.strikeouts()).isEqualTo(9);
        assertThat(result.stolenBases()).isEqualTo(8);
        assertThat(result.battingAvg()).isEqualTo(0.25);
    }

    @Test
    @DisplayName("returns a batting average of 0.000 when at-bats is zero")
    void should_return_zero_batting_avg_when_at_bats_is_zero() throws Exception {
        // given
        playerStatsRepository.save(
                new PlayerStats(7, "Rookie", "Bench", 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));

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
