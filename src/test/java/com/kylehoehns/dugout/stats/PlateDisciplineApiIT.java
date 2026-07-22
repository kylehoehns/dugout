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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

// Several assertions below intentionally depend on the seeded values in
// src/main/resources/roster-stats.csv (per the spec's acceptance examples), so editing
// the CSV will require updating these tests accordingly.
@SpringBootTest
@AutoConfigureMockMvc
class PlateDisciplineApiIT {

    @Autowired MockMvc mockMvc;

    @Autowired ObjectMapper objectMapper;

    @Autowired PlayerStatsRepository playerStatsRepository;

    @Test
    @DisplayName(
            "returns all twelve qualifying players ranked by eye descending when listing plate"
                    + " discipline")
    void should_return_all_players_ranked_by_eye_descending_when_listing_plate_discipline()
            throws Exception {
        // given - the CSV-seeded roster of 12 players, all of whom qualify (PA >= 10)

        // when
        var response = performGetPlateDiscipline();
        var discipline = parsePlateDiscipline(response);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(discipline).hasSize(12);
        assertThat(discipline)
                .extracting(PlateDisciplineResponse::eye)
                .isSortedAccordingTo(Comparator.reverseOrder());

        assertThat(discipline[0].jerseyNumber()).isEqualTo(23);
        assertThat(discipline[0].name()).isEqualTo("Mason Reed");
        assertThat(discipline[0].walks()).isEqualTo(12);
        assertThat(discipline[0].strikeouts()).isEqualTo(1);
        assertThat(discipline[0].eye()).isEqualByComparingTo(new BigDecimal("12.000"));

        assertThat(discipline)
                .extracting(PlateDisciplineResponse::jerseyNumber)
                .containsExactly(23, 92, 86, 36, 64, 45, 4, 11, 99, 1, 12, 16);
    }

    @Test
    @DisplayName("includes Jonah West #45 the patient walker with an eye of 1.158")
    void should_include_patient_walker_when_listing_plate_discipline() throws Exception {
        // given - Jonah West #45 has 29 AB and 22 BB, for 51 plate appearances

        // when
        var discipline = parsePlateDiscipline(performGetPlateDiscipline());

        // then
        assertThat(discipline)
                .filteredOn(entry -> entry.jerseyNumber().equals(45))
                .singleElement()
                .satisfies(
                        jonahWest -> {
                            assertThat(jonahWest.name()).isEqualTo("Jonah West");
                            assertThat(jonahWest.walks()).isEqualTo(22);
                            assertThat(jonahWest.strikeouts()).isEqualTo(19);
                            assertThat(jonahWest.eye())
                                    .isEqualByComparingTo(new BigDecimal("1.158"));
                        });
    }

    @Test
    @DisplayName("returns an eye of 8.000 and never divides by zero when strikeouts is zero")
    void should_return_eye_without_dividing_by_zero_when_strikeouts_is_zero() throws Exception {
        // given
        int jerseyNumber = 501;
        var zeroStrikeoutPlayer = newPlayerStats(jerseyNumber, "Zero", "Whiff", 10, 10, 5, 8, 0);

        // when / then
        withTemporaryPlayer(
                zeroStrikeoutPlayer,
                () -> {
                    var response = performGetPlateDiscipline();
                    var discipline = parsePlateDiscipline(response);

                    assertThat(response.getStatus()).isEqualTo(200);
                    assertThat(discipline)
                            .filteredOn(entry -> entry.jerseyNumber().equals(jerseyNumber))
                            .singleElement()
                            .satisfies(
                                    zeroWhiff -> {
                                        assertThat(zeroWhiff.walks()).isEqualTo(8);
                                        assertThat(zeroWhiff.strikeouts()).isEqualTo(0);
                                        assertThat(zeroWhiff.eye())
                                                .isEqualByComparingTo(new BigDecimal("8.000"));
                                    });
                });
    }

    @Test
    @DisplayName(
            "excludes a player with fewer than 10 plate appearances from the plate discipline"
                    + " leaderboard")
    void should_exclude_player_when_plate_appearances_is_below_the_qualifying_threshold()
            throws Exception {
        // given
        int jerseyNumber = 502;
        var subThresholdPlayer = newPlayerStats(jerseyNumber, "Small", "Sample", 3, 3, 1, 2, 1);

        // when / then
        withTemporaryPlayer(
                subThresholdPlayer,
                () -> {
                    var response = performGetPlateDiscipline();
                    var discipline = parsePlateDiscipline(response);

                    assertThat(response.getStatus()).isEqualTo(200);
                    assertThat(discipline)
                            .extracting(PlateDisciplineResponse::jerseyNumber)
                            .doesNotContain(jerseyNumber);
                });
    }

    @Test
    @DisplayName(
            "breaks eye ties by walks descending, then jersey number ascending, when listing"
                    + " plate discipline")
    void should_break_eye_ties_by_walks_then_jersey_number_when_listing_plate_discipline()
            throws Exception {
        // given - three qualifying players that tie on eye (1.000); two of those also tie on
        // walks, so the sort must fall through to jerseyNumber ascending as the final tie-break
        int lowerJerseySameWalks = 700;
        int higherJerseySameWalks = 701;
        int fewerWalksJersey = 702;
        var lowerJersey = newPlayerStats(lowerJerseySameWalks, "Low", "Jersey", 10, 10, 5, 10, 10);
        var higherJersey =
                newPlayerStats(higherJerseySameWalks, "High", "Jersey", 10, 10, 5, 10, 10);
        var fewerWalks = newPlayerStats(fewerWalksJersey, "Fewer", "Walks", 10, 10, 5, 6, 6);

        // when / then
        withTemporaryPlayer(
                lowerJersey,
                () ->
                        withTemporaryPlayer(
                                higherJersey,
                                () ->
                                        withTemporaryPlayer(
                                                fewerWalks,
                                                () -> {
                                                    var response = performGetPlateDiscipline();
                                                    var discipline = parsePlateDiscipline(response);

                                                    assertThat(response.getStatus()).isEqualTo(200);

                                                    int lowerJerseyIndex =
                                                            indexOfJersey(
                                                                    discipline,
                                                                    lowerJerseySameWalks);
                                                    int higherJerseyIndex =
                                                            indexOfJersey(
                                                                    discipline,
                                                                    higherJerseySameWalks);
                                                    int fewerWalksIndex =
                                                            indexOfJersey(
                                                                    discipline, fewerWalksJersey);

                                                    assertThat(lowerJerseyIndex)
                                                            .isLessThan(higherJerseyIndex);
                                                    assertThat(higherJerseyIndex)
                                                            .isLessThan(fewerWalksIndex);
                                                })));
    }

    @Test
    @DisplayName("does not leak the eye field into the existing player stats responses")
    void should_not_leak_eye_field_when_requesting_existing_player_stats_endpoints()
            throws Exception {
        // given - the existing /api/stats endpoints are unchanged by the new leaderboard

        // when
        var allStatsResponse = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var singleStatsResponse =
                mockMvc.perform(get("/api/stats/{number}", 23)).andReturn().getResponse();

        // then
        assertThat(allStatsResponse.getStatus()).isEqualTo(200);
        assertThat(allStatsResponse.getContentAsString()).doesNotContain("\"eye\"");
        assertThat(singleStatsResponse.getStatus()).isEqualTo(200);
        assertThat(singleStatsResponse.getContentAsString()).doesNotContain("\"eye\"");
    }

    private MockHttpServletResponse performGetPlateDiscipline() throws Exception {
        return mockMvc.perform(get("/api/stats/plate-discipline")).andReturn().getResponse();
    }

    private PlateDisciplineResponse[] parsePlateDiscipline(MockHttpServletResponse response)
            throws Exception {
        return objectMapper.readValue(
                response.getContentAsString(), PlateDisciplineResponse[].class);
    }

    private static int indexOfJersey(PlateDisciplineResponse[] discipline, int jerseyNumber) {
        for (int i = 0; i < discipline.length; i++) {
            if (discipline[i].jerseyNumber().equals(jerseyNumber)) {
                return i;
            }
        }
        throw new AssertionError("Jersey number " + jerseyNumber + " not found in response");
    }

    /**
     * Saves the given player for the duration of {@code test}, then deletes it, so each test leaves
     * the seeded roster untouched regardless of how the assertions turn out.
     */
    private void withTemporaryPlayer(PlayerStats player, ThrowingRunnable test) throws Exception {
        playerStatsRepository.save(player);
        try {
            test.run();
        } finally {
            playerStatsRepository.deleteById(player.getJerseyNumber());
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static PlayerStats newPlayerStats(
            int jerseyNumber,
            String firstName,
            String lastName,
            int gamesPlayed,
            int atBats,
            int hits,
            int walks,
            int strikeouts) {
        var player = new PlayerStats();
        player.setJerseyNumber(jerseyNumber);
        player.setFirstName(firstName);
        player.setLastName(lastName);
        player.setGamesPlayed(gamesPlayed);
        player.setAtBats(atBats);
        player.setHits(hits);
        player.setDoubles(0);
        player.setTriples(0);
        player.setHomeRuns(0);
        player.setRbi(0);
        player.setRuns(0);
        player.setWalks(walks);
        player.setStrikeouts(strikeouts);
        player.setStolenBases(0);
        return player;
    }
}
