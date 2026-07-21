package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
import java.util.List;
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
class TeamSummaryApiIT {

    @Autowired MockMvc mockMvc;

    @Autowired ObjectMapper objectMapper;

    @Autowired PlayerStatsRepository playerStatsRepository;

    @Test
    @DisplayName(
            "returns the pooled team batting average of 0.438 and all twelve seeded players when"
                    + " everyone qualifies")
    void should_return_pooled_team_average_and_all_players_when_all_qualify() throws Exception {
        // given - the CSV-seeded roster of 12 players, all with atBats >= 10

        // when
        var response = mockMvc.perform(get("/api/stats/team-summary")).andReturn().getResponse();
        var summary =
                objectMapper.readValue(response.getContentAsString(), TeamSummaryResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(summary.teamBattingAvg()).isEqualByComparingTo(new BigDecimal("0.438"));
        assertThat(summary.players()).hasSize(12);
        assertThat(summary.players().get(0).jerseyNumber()).isEqualTo(92);
        assertThat(summary.players().get(0).name()).isEqualTo("Cooper Lane");
        assertThat(summary.players().get(0).hits()).isEqualTo(26);
        assertThat(summary.players().get(0).battingAvg())
                .isEqualByComparingTo(new BigDecimal("0.578"));
    }

    @Test
    @DisplayName(
            "sorts jersey 36 Owen Bell before jersey 16 Brody Vance when both have 14 hits but"
                    + " Bell's batting average is higher")
    void should_sort_by_batting_average_when_hits_are_tied() throws Exception {
        // given - the CSV-seeded roster where #36 Owen Bell and #16 Brody Vance both have 14 hits

        // when
        var response = mockMvc.perform(get("/api/stats/team-summary")).andReturn().getResponse();
        var summary =
                objectMapper.readValue(response.getContentAsString(), TeamSummaryResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        var jerseyNumbers =
                summary.players().stream().map(PlayerStatsResponse::jerseyNumber).toList();
        int owenBellIndex = jerseyNumbers.indexOf(36);
        int brodyVanceIndex = jerseyNumbers.indexOf(16);
        assertThat(owenBellIndex).isGreaterThanOrEqualTo(0);
        assertThat(brodyVanceIndex).isGreaterThanOrEqualTo(0);
        assertThat(owenBellIndex).isLessThan(brodyVanceIndex);
        assertThat(summary.players().get(owenBellIndex).battingAvg())
                .isEqualByComparingTo(new BigDecimal("0.519"));
        assertThat(summary.players().get(brodyVanceIndex).battingAvg())
                .isEqualByComparingTo(new BigDecimal("0.368"));
    }

    @Test
    @DisplayName(
            "excludes a player with fewer than 10 at-bats from both the list and the team average")
    void should_exclude_player_when_at_bats_is_below_threshold() throws Exception {
        // given - a below-threshold 1-for-1 line added alongside the seeded, all-qualifying
        // roster
        int jerseyNumber = 501;
        var smallSamplePlayer = newPlayerStats(jerseyNumber, "Small", "Sample", 1, 1, 1);
        playerStatsRepository.save(smallSamplePlayer);

        try {
            // when
            var response =
                    mockMvc.perform(get("/api/stats/team-summary")).andReturn().getResponse();
            var summary =
                    objectMapper.readValue(
                            response.getContentAsString(), TeamSummaryResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(summary.players()).hasSize(12);
            assertThat(summary.players())
                    .extracting(PlayerStatsResponse::jerseyNumber)
                    .doesNotContain(jerseyNumber);
            assertThat(summary.teamBattingAvg()).isEqualByComparingTo(new BigDecimal("0.438"));
        } finally {
            playerStatsRepository.deleteById(jerseyNumber);
        }
    }

    @Test
    @DisplayName(
            "returns a team batting average of 0.000 and an empty players list without dividing by"
                    + " zero when no player qualifies")
    void should_return_zero_average_and_empty_players_when_no_player_qualifies() throws Exception {
        // given - the shared roster is temporarily replaced with a single below-threshold player
        List<PlayerStats> originalRoster = playerStatsRepository.findAll();
        playerStatsRepository.deleteAll();
        var smallSamplePlayer = newPlayerStats(502, "Tiny", "Sample", 1, 2, 1);
        playerStatsRepository.save(smallSamplePlayer);

        try {
            // when
            var response =
                    mockMvc.perform(get("/api/stats/team-summary")).andReturn().getResponse();
            var summary =
                    objectMapper.readValue(
                            response.getContentAsString(), TeamSummaryResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(summary.teamBattingAvg()).isEqualByComparingTo(new BigDecimal("0.000"));
            assertThat(summary.players()).isEmpty();
        } finally {
            playerStatsRepository.deleteAll();
            playerStatsRepository.saveAll(originalRoster);
        }
    }

    private static PlayerStats newPlayerStats(
            int jerseyNumber,
            String firstName,
            String lastName,
            int gamesPlayed,
            int atBats,
            int hits) {
        var playerStats = new PlayerStats();
        playerStats.setJerseyNumber(jerseyNumber);
        playerStats.setFirstName(firstName);
        playerStats.setLastName(lastName);
        playerStats.setGamesPlayed(gamesPlayed);
        playerStats.setAtBats(atBats);
        playerStats.setHits(hits);
        playerStats.setDoubles(0);
        playerStats.setTriples(0);
        playerStats.setHomeRuns(0);
        playerStats.setRbi(0);
        playerStats.setRuns(0);
        playerStats.setWalks(0);
        playerStats.setStrikeouts(0);
        playerStats.setStolenBases(0);
        return playerStats;
    }
}
