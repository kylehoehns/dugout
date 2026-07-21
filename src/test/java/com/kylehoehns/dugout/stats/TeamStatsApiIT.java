package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
import java.util.Comparator;
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
class TeamStatsApiIT {

    @Autowired MockMvc mockMvc;

    @Autowired ObjectMapper objectMapper;

    @Autowired PlayerStatsRepository playerStatsRepository;

    @Test
    @DisplayName(
            "returns the team batting average and all twelve eligible players when getting team stats")
    void should_return_team_batting_average_and_all_players_when_getting_team_stats()
            throws Exception {
        // given - the CSV-seeded roster of 12 players, all with atBats >= 10

        // when
        var response = mockMvc.perform(get("/api/stats/team")).andReturn().getResponse();
        var teamStats =
                objectMapper.readValue(response.getContentAsString(), TeamStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(teamStats.teamBattingAvg()).isEqualByComparingTo(new BigDecimal("0.438"));
        assertThat(teamStats.players()).hasSize(12);
    }

    @Test
    @DisplayName("pins the hits leader first even above a teammate with a higher batting average")
    void should_pin_hits_leader_first_when_teammate_has_higher_batting_average() throws Exception {
        // given - Cooper Lane (jersey 92) leads the team in hits, but Mason Reed (jersey 23) has
        // a higher batting average

        // when
        var response = mockMvc.perform(get("/api/stats/team")).andReturn().getResponse();
        var teamStats =
                objectMapper.readValue(response.getContentAsString(), TeamStatsResponse.class);
        var players = teamStats.players();

        // then
        assertThat(players.get(0).jerseyNumber()).isEqualTo(92);
        assertThat(players.get(0).name()).isEqualTo("Cooper Lane");
        assertThat(players.get(0).hits()).isEqualTo(26);
        assertThat(players.get(0).battingAvg()).isEqualByComparingTo(new BigDecimal("0.578"));

        assertThat(players.get(1).jerseyNumber()).isEqualTo(23);
        assertThat(players.get(1).name()).isEqualTo("Mason Reed");
        assertThat(players.get(1).hits()).isEqualTo(25);
        assertThat(players.get(1).battingAvg()).isEqualByComparingTo(new BigDecimal("0.610"));

        // the hits leader is pinned above a teammate with a strictly higher batting average
        assertThat(players.get(1).battingAvg()).isGreaterThan(players.get(0).battingAvg());
    }

    @Test
    @DisplayName("sorts the players after the pinned hits leader by batting average descending")
    void should_sort_players_after_pinned_leader_by_batting_average_descending() throws Exception {
        // given - the pinned hits leader occupies index 0

        // when
        var response = mockMvc.perform(get("/api/stats/team")).andReturn().getResponse();
        var teamStats =
                objectMapper.readValue(response.getContentAsString(), TeamStatsResponse.class);
        var tail = teamStats.players().subList(1, teamStats.players().size());

        // then
        assertThat(tail)
                .extracting(PlayerStatsResponse::battingAvg)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @DisplayName(
            "excludes a fluke player under 10 at-bats from the roster and leaves the team average"
                    + " unchanged")
    void should_exclude_ineligible_player_and_leave_team_average_unchanged_when_at_bats_below_ten()
            throws Exception {
        // given
        int jerseyNumber = 500;
        var flukePlayer = new PlayerStats();
        flukePlayer.setJerseyNumber(jerseyNumber);
        flukePlayer.setFirstName("Fluke");
        flukePlayer.setLastName("Player");
        flukePlayer.setGamesPlayed(1);
        flukePlayer.setAtBats(1);
        flukePlayer.setHits(1);
        flukePlayer.setDoubles(0);
        flukePlayer.setTriples(0);
        flukePlayer.setHomeRuns(0);
        flukePlayer.setRbi(0);
        flukePlayer.setRuns(0);
        flukePlayer.setWalks(0);
        flukePlayer.setStrikeouts(0);
        flukePlayer.setStolenBases(0);
        playerStatsRepository.save(flukePlayer);

        try {
            // when
            var response = mockMvc.perform(get("/api/stats/team")).andReturn().getResponse();
            var teamStats =
                    objectMapper.readValue(response.getContentAsString(), TeamStatsResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(teamStats.players())
                    .extracting(PlayerStatsResponse::jerseyNumber)
                    .doesNotContain(jerseyNumber);
            assertThat(teamStats.players()).hasSize(12);
            assertThat(teamStats.teamBattingAvg()).isEqualByComparingTo(new BigDecimal("0.438"));
        } finally {
            playerStatsRepository.deleteById(jerseyNumber);
        }
    }

    @Test
    @DisplayName("breaks a tie on hits by putting the player with the higher batting average first")
    void should_break_hits_tie_by_higher_batting_average_when_two_players_share_the_most_hits()
            throws Exception {
        // given - two synthetic players tied on hits (30, higher than any seeded player), one
        // with a clearly higher batting average
        int higherAvgJersey = 501;
        int lowerAvgJersey = 502;
        var higherAvgPlayer =
                new PlayerStats(
                        higherAvgJersey, "Tied", "High", 10, 40, 30, 0, 0, 0, 0, 0, 0, 0, 0);
        var lowerAvgPlayer =
                new PlayerStats(lowerAvgJersey, "Tied", "Low", 10, 50, 30, 0, 0, 0, 0, 0, 0, 0, 0);
        playerStatsRepository.save(higherAvgPlayer);
        playerStatsRepository.save(lowerAvgPlayer);

        try {
            // when
            var response = mockMvc.perform(get("/api/stats/team")).andReturn().getResponse();
            var teamStats =
                    objectMapper.readValue(response.getContentAsString(), TeamStatsResponse.class);

            // then
            var leader = teamStats.players().get(0);
            assertThat(leader.jerseyNumber()).isEqualTo(higherAvgJersey);
            assertThat(leader.hits()).isEqualTo(30);
            assertThat(leader.battingAvg()).isEqualByComparingTo(new BigDecimal("0.750"));
        } finally {
            playerStatsRepository.deleteById(higherAvgJersey);
            playerStatsRepository.deleteById(lowerAvgJersey);
        }
    }

    @Test
    @DisplayName("returns a team average of 0.000 and an empty player list when no one is eligible")
    void should_return_zero_average_and_empty_players_when_no_one_is_eligible() throws Exception {
        // given - the whole roster is temporarily removed so no player is eligible
        List<PlayerStats> originalRoster = playerStatsRepository.findAll();
        playerStatsRepository.deleteAll();

        try {
            // when
            var response = mockMvc.perform(get("/api/stats/team")).andReturn().getResponse();
            var teamStats =
                    objectMapper.readValue(response.getContentAsString(), TeamStatsResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(teamStats.teamBattingAvg()).isEqualByComparingTo(new BigDecimal("0.000"));
            assertThat(teamStats.players()).isEmpty();
        } finally {
            playerStatsRepository.saveAll(originalRoster);
        }
    }
}
