package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

// Several assertions below intentionally depend on the seeded values in
// src/main/resources/roster-stats.csv, so editing the CSV will require updating these
// tests accordingly.
//
// Note: the spec's (docs/plate-discipline-spec.md) worked eye numbers for players other
// than #23 Mason Reed do not reproduce from the actual seeded CSV under the walks (BB)/
// strikeouts (SO) columns already wired up by RosterStatsLoader/PlayerStats - e.g. the
// spec's own tiebreak example asks for Landon Cross at 13 BB / 26 SO and Brody Vance at
// 9 BB / 18 SO, but the seeded row for Cross is 13 BB / 10 SO. Below we assert the real,
// correctly-computed values from the existing seeded columns instead of the spec's
// figures, and cover the tiebreak/SO=0/0-0 behaviors with self-seeded players (as the
// spec itself directs for the SO=0 and 0/0 cases, since the seeded roster has neither).
@SpringBootTest
@AutoConfigureMockMvc
class PlateDisciplineApiIT {

    @Autowired MockMvc mockMvc;

    @Autowired ObjectMapper objectMapper;

    @Autowired PlayerStatsRepository playerStatsRepository;

    @Test
    @DisplayName("returns all twelve qualified players when ranking plate discipline")
    void should_return_all_twelve_qualified_players_when_ranking_plate_discipline()
            throws Exception {
        // given - the CSV-seeded roster of 12 players, all with atBats >= 10

        // when
        var response =
                mockMvc.perform(get("/api/stats/plate-discipline")).andReturn().getResponse();
        var ranking =
                objectMapper.readValue(
                        response.getContentAsString(), PlateDisciplineResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(ranking).hasSize(12);
    }

    @Test
    @DisplayName("ranks players by eye descending with jersey 23 Mason Reed first")
    void should_rank_players_by_eye_descending_when_listing_plate_discipline() throws Exception {
        // given - the CSV-seeded roster, all with atBats >= 10

        // when
        var response =
                mockMvc.perform(get("/api/stats/plate-discipline")).andReturn().getResponse();
        var ranking =
                objectMapper.readValue(
                        response.getContentAsString(), PlateDisciplineResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(ranking[0].jerseyNumber()).isEqualTo(23);
        assertThat(ranking[0].name()).isEqualTo("Mason Reed");
        assertThat(ranking[0].walks()).isEqualTo(12);
        assertThat(ranking[0].strikeouts()).isEqualTo(1);
        assertThat(ranking[0].eye()).isEqualByComparingTo(new BigDecimal("12.000"));
        assertThat(ranking[1].jerseyNumber()).isEqualTo(92);
        assertThat(ranking[1].eye()).isEqualByComparingTo(new BigDecimal("2.000"));
        assertThat(ranking[2].jerseyNumber()).isEqualTo(86);
        assertThat(ranking[2].eye()).isEqualByComparingTo(new BigDecimal("1.636"));
        assertThat(ranking[ranking.length - 1].jerseyNumber()).isEqualTo(16);
        assertThat(ranking[ranking.length - 1].eye()).isEqualByComparingTo(new BigDecimal("0.500"));
    }

    @Test
    @DisplayName("sorts the player with more walks first when two players tie on eye")
    void should_sort_by_walks_when_eye_is_tied() throws Exception {
        // given - two qualified players who both post an eye of 0.500, but jersey 601 has more
        // walks than jersey 602
        var morePatientPlayer = newPlayerStats(601, "More", "Patient", 20, 10, 20);
        var lessPatientPlayer = newPlayerStats(602, "Less", "Patient", 20, 5, 10);
        playerStatsRepository.saveAll(List.of(morePatientPlayer, lessPatientPlayer));

        try {
            // when
            var response =
                    mockMvc.perform(get("/api/stats/plate-discipline")).andReturn().getResponse();
            var ranking =
                    objectMapper.readValue(
                            response.getContentAsString(), PlateDisciplineResponse[].class);

            // then
            assertThat(response.getStatus()).isEqualTo(200);
            var jerseyNumbers =
                    Arrays.stream(ranking).map(PlateDisciplineResponse::jerseyNumber).toList();
            int morePatientIndex = jerseyNumbers.indexOf(601);
            int lessPatientIndex = jerseyNumbers.indexOf(602);
            assertThat(morePatientIndex).isGreaterThanOrEqualTo(0);
            assertThat(lessPatientIndex).isGreaterThanOrEqualTo(0);
            assertThat(morePatientIndex).isLessThan(lessPatientIndex);
            assertThat(ranking[morePatientIndex].eye())
                    .isEqualByComparingTo(new BigDecimal("0.500"));
            assertThat(ranking[lessPatientIndex].eye())
                    .isEqualByComparingTo(new BigDecimal("0.500"));
        } finally {
            playerStatsRepository.deleteById(601);
            playerStatsRepository.deleteById(602);
        }
    }

    @Test
    @DisplayName("treats zero strikeouts as one and ranks the player first with eye equal to walks")
    void should_divide_by_one_and_rank_first_when_strikeouts_is_zero() throws Exception {
        // given - a qualified player who has walked 15 times and never struck out, more walks
        // than jersey 23 Mason Reed's existing 12.000 (unique-max) seeded eye, so this new
        // player becomes the new unique max and sorts to the top
        int jerseyNumber = 603;
        var neverStrikesOut = newPlayerStats(jerseyNumber, "Never", "StrikesOut", 20, 15, 0);
        playerStatsRepository.save(neverStrikesOut);

        try {
            // when
            var response =
                    mockMvc.perform(get("/api/stats/plate-discipline")).andReturn().getResponse();
            var ranking =
                    objectMapper.readValue(
                            response.getContentAsString(), PlateDisciplineResponse[].class);

            // then
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(ranking[0].jerseyNumber()).isEqualTo(jerseyNumber);
            assertThat(ranking[0].walks()).isEqualTo(15);
            assertThat(ranking[0].strikeouts()).isEqualTo(0);
            assertThat(ranking[0].eye()).isEqualByComparingTo(new BigDecimal("15.000"));
        } finally {
            playerStatsRepository.deleteById(jerseyNumber);
        }
    }

    @Test
    @DisplayName(
            "returns an eye of 0.000 without dividing by zero when walks and strikeouts are"
                    + " both zero")
    void should_return_zero_eye_when_walks_and_strikeouts_are_both_zero() throws Exception {
        // given - a qualified player who has neither walked nor struck out
        int jerseyNumber = 604;
        var noWalksNoStrikeouts = newPlayerStats(jerseyNumber, "No", "PlateDiscipline", 20, 0, 0);
        playerStatsRepository.save(noWalksNoStrikeouts);

        try {
            // when
            var response =
                    mockMvc.perform(get("/api/stats/plate-discipline")).andReturn().getResponse();
            var ranking =
                    objectMapper.readValue(
                            response.getContentAsString(), PlateDisciplineResponse[].class);

            // then
            assertThat(response.getStatus()).isEqualTo(200);
            var jerseyNumbers =
                    Arrays.stream(ranking).map(PlateDisciplineResponse::jerseyNumber).toList();
            int index = jerseyNumbers.indexOf(jerseyNumber);
            assertThat(index).isGreaterThanOrEqualTo(0);
            assertThat(ranking[index].eye()).isEqualByComparingTo(new BigDecimal("0.000"));
        } finally {
            playerStatsRepository.deleteById(jerseyNumber);
        }
    }

    @Test
    @DisplayName(
            "does not throw when a qualified player has a null walks value tied on eye with"
                    + " another player")
    void should_not_throw_when_walks_is_null_and_tied_on_eye() throws Exception {
        // given - two qualified players tied at eye 0.000: jersey 606 has null walks, jersey 607
        // has walks explicitly set to zero, so the walks-tiebreak comparator must null-guard
        int nullWalksJersey = 606;
        int zeroWalksJersey = 607;
        var nullWalksPlayer = newPlayerStats(nullWalksJersey, "Null", "Walks", 20, 0, 5);
        nullWalksPlayer.setWalks(null);
        var zeroWalksPlayer = newPlayerStats(zeroWalksJersey, "Zero", "Walks", 20, 0, 5);
        playerStatsRepository.saveAll(List.of(nullWalksPlayer, zeroWalksPlayer));

        try {
            // when
            var response =
                    mockMvc.perform(get("/api/stats/plate-discipline")).andReturn().getResponse();
            var ranking =
                    objectMapper.readValue(
                            response.getContentAsString(), PlateDisciplineResponse[].class);

            // then
            assertThat(response.getStatus()).isEqualTo(200);
            var jerseyNumbers =
                    Arrays.stream(ranking).map(PlateDisciplineResponse::jerseyNumber).toList();
            assertThat(jerseyNumbers).contains(nullWalksJersey, zeroWalksJersey);
            int nullWalksIndex = jerseyNumbers.indexOf(nullWalksJersey);
            int zeroWalksIndex = jerseyNumbers.indexOf(zeroWalksJersey);
            assertThat(nullWalksIndex).isLessThan(zeroWalksIndex);
        } finally {
            playerStatsRepository.deleteById(nullWalksJersey);
            playerStatsRepository.deleteById(zeroWalksJersey);
        }
    }

    @Test
    @DisplayName("excludes a player with fewer than 10 at-bats from the plate-discipline ranking")
    void should_exclude_player_when_at_bats_is_below_threshold() throws Exception {
        // given - a below-threshold 1-for-1 line added alongside the seeded, all-qualifying
        // roster
        int jerseyNumber = 605;
        var smallSamplePlayer = newPlayerStats(jerseyNumber, "Small", "Sample", 1, 3, 1);
        playerStatsRepository.save(smallSamplePlayer);

        try {
            // when
            var response =
                    mockMvc.perform(get("/api/stats/plate-discipline")).andReturn().getResponse();
            var ranking =
                    objectMapper.readValue(
                            response.getContentAsString(), PlateDisciplineResponse[].class);

            // then
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(ranking).hasSize(12);
            assertThat(ranking)
                    .extracting(PlateDisciplineResponse::jerseyNumber)
                    .doesNotContain(jerseyNumber);
        } finally {
            playerStatsRepository.deleteById(jerseyNumber);
        }
    }

    private static PlayerStats newPlayerStats(
            int jerseyNumber,
            String firstName,
            String lastName,
            int atBats,
            int walks,
            int strikeouts) {
        var playerStats = new PlayerStats();
        playerStats.setJerseyNumber(jerseyNumber);
        playerStats.setFirstName(firstName);
        playerStats.setLastName(lastName);
        playerStats.setGamesPlayed(10);
        playerStats.setAtBats(atBats);
        playerStats.setHits(0);
        playerStats.setDoubles(0);
        playerStats.setTriples(0);
        playerStats.setHomeRuns(0);
        playerStats.setRbi(0);
        playerStats.setRuns(0);
        playerStats.setWalks(walks);
        playerStats.setStrikeouts(strikeouts);
        playerStats.setStolenBases(0);
        return playerStats;
    }
}
