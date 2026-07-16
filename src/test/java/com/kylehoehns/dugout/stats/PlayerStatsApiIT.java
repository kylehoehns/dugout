package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import org.junit.jupiter.api.AfterEach;
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

    private static final int BENCH_WARMER_JERSEY_NUMBER = 500;

    @Autowired MockMvc mockMvc;

    @Autowired ObjectMapper objectMapper;

    @Autowired PlayerStatsRepository playerStatsRepository;

    @AfterEach
    void tearDown() {
        playerStatsRepository
                .findById(BENCH_WARMER_JERSEY_NUMBER)
                .ifPresent(playerStatsRepository::delete);
    }

    @Test
    @DisplayName("returns all 12 seeded players sorted by batting average descending")
    void should_return_all_players_sorted_by_batting_average_descending_when_listing()
            throws Exception {
        // given / when
        var response = mockMvc.perform(get("/api/stats")).andReturn().getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse[].class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats).hasSize(12);
        assertThat(stats)
                .isSortedAccordingTo(
                        Comparator.comparing(PlayerStatsResponse::battingAvg).reversed());
        var highestBattingAvg =
                Arrays.stream(stats)
                        .map(PlayerStatsResponse::battingAvg)
                        .max(Comparator.naturalOrder())
                        .orElseThrow();
        assertThat(stats[0].battingAvg()).isEqualByComparingTo(highestBattingAvg);
    }

    @Test
    @DisplayName("returns Tate Hoehns's stats when the jersey number is 4")
    void should_return_tate_hoehns_stats_when_jersey_number_is_4() throws Exception {
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
    @DisplayName("returns stolen bases when the jersey number is 92")
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
    @DisplayName("returns a batting average of 0.000 when at-bats is zero")
    void should_return_zero_batting_average_when_at_bats_is_zero() throws Exception {
        // given
        playerStatsRepository.save(
                new PlayerStats(
                        BENCH_WARMER_JERSEY_NUMBER,
                        "Bench",
                        "Warmer",
                        5,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0));

        // when
        var response =
                mockMvc.perform(get("/api/stats/{number}", BENCH_WARMER_JERSEY_NUMBER))
                        .andReturn()
                        .getResponse();
        var stats =
                objectMapper.readValue(response.getContentAsString(), PlayerStatsResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(stats.atBats()).isEqualTo(0);
        assertThat(stats.battingAvg()).isEqualByComparingTo(new BigDecimal("0.000"));
    }
}
