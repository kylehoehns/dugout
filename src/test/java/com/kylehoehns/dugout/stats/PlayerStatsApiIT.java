package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
import java.util.Comparator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

// Several assertions below intentionally depend on the seeded values in
// src/main/resources/roster-stats.csv (per the spec's acceptance examples), so editing
// the CSV will require updating these tests accordingly.
@SpringBootTest
@AutoConfigureMockMvc
class PlayerStatsApiIT {

    @Autowired MockMvc mockMvc;

    @Autowired ObjectMapper objectMapper;

    @Autowired PlayerStatsRepository playerStatsRepository;

    @Test
    @DisplayName("returns all twelve seeded players sorted by batting average descending")
    void should_return_all_players_sorted_by_batting_average_descending_when_listing()
            throws Exception {
        // given - the CSV-seeded roster of 12 players loaded on startup

        // when
        var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats).hasSize(12);
        assertThat(stats)
                .extracting(PlayerStatsResponse::battingAvg)
                .isSortedAccordingTo(Comparator.reverseOrder());
        assertThat(stats[0].jerseyNumber()).isEqualTo(23);
        assertThat(stats[0].name()).isEqualTo("Mason Reed");
        assertThat(stats[0].battingAvg()).isEqualByComparingTo(new BigDecimal("0.610"));
    }

    @Test
    @DisplayName(
            "returns Tate Hoehns' stats with a batting average of 0.429 when jersey 4 is requested")
    void should_return_player_stats_when_jersey_number_is_4() throws Exception {
        // given
        int jerseyNumber = 4;

        // when
        var response =
                mockMvc.perform(get("/api/stats/{number}", jerseyNumber)).andReturn().getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats.name()).isEqualTo("Tate Hoehns");
        assertThat(stats.atBats()).isEqualTo(28);
        assertThat(stats.hits()).isEqualTo(12);
        assertThat(stats.battingAvg()).isEqualByComparingTo(new BigDecimal("0.429"));
    }

    @Test
    @DisplayName("returns stolen bases of 41 when jersey 92 is requested")
    void should_return_stolen_bases_when_jersey_number_is_92() throws Exception {
        // given
        int jerseyNumber = 92;

        // when
        var response =
                mockMvc.perform(get("/api/stats/{number}", jerseyNumber)).andReturn().getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats.stolenBases()).isEqualTo(41);
    }

    @Test
    @DisplayName("returns 404 when the jersey number is unknown")
    void should_return_404_when_jersey_number_is_unknown() throws Exception {
        // given
        int unknownJerseyNumber = 777;

        // when
        var status =
                mockMvc.perform(get("/api/stats/{number}", unknownJerseyNumber))
                        .andReturn()
                        .getResponse()
                        .getStatus();

        // then
        assertThat(status).isEqualTo(404);
    }

    @Test
    @DisplayName("returns a batting average of 0.000 and never divides by zero when at-bats is 0")
    void should_return_zero_batting_average_when_at_bats_is_zero() throws Exception {
        // given
        int jerseyNumber = 500;
        var benchPlayer = new PlayerStats();
        benchPlayer.setJerseyNumber(jerseyNumber);
        benchPlayer.setFirstName("Bench");
        benchPlayer.setLastName("Player");
        benchPlayer.setGamesPlayed(5);
        benchPlayer.setAtBats(0);
        benchPlayer.setHits(0);
        benchPlayer.setDoubles(0);
        benchPlayer.setTriples(0);
        benchPlayer.setHomeRuns(0);
        benchPlayer.setRbi(0);
        benchPlayer.setRuns(0);
        benchPlayer.setWalks(0);
        benchPlayer.setStrikeouts(0);
        benchPlayer.setStolenBases(0);
        playerStatsRepository.save(benchPlayer);

        try {
            // when
            var response =
                    mockMvc.perform(get("/api/stats/{number}", jerseyNumber))
                            .andReturn()
                            .getResponse();
            var stats =
                    objectMapper.readValue(
                            response.getContentAsString(), PlayerStatsResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(stats.atBats()).isEqualTo(0);
            assertThat(stats.hits()).isEqualTo(0);
            assertThat(stats.battingAvg()).isEqualByComparingTo(new BigDecimal("0.000"));
        } finally {
            playerStatsRepository.deleteById(jerseyNumber);
        }
    }
}
