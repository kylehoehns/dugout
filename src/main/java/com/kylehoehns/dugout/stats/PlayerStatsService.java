package com.kylehoehns.dugout.stats;

import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlayerStatsService {

    private static final int QUALIFYING_AT_BATS = 10;

    private final PlayerStatsRepository playerStatsRepository;

    public PlayerStatsService(PlayerStatsRepository playerStatsRepository) {
        this.playerStatsRepository = playerStatsRepository;
    }

    public List<PlayerStatsResponse> getAllPlayerStats() {
        return playerStatsRepository.findAll().stream()
                .map(PlayerStatsResponse::from)
                .sorted(Comparator.comparing(PlayerStatsResponse::battingAvg).reversed())
                .toList();
    }

    public PlayerStatsResponse getPlayerStatsByJerseyNumber(Integer jerseyNumber) {
        return playerStatsRepository
                .findById(jerseyNumber)
                .map(PlayerStatsResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<PlateDisciplineResponse> getPlateDisciplineRanking() {
        return playerStatsRepository.findAll().stream()
                .filter(this::isQualified)
                .map(PlayerStatsResponse::from)
                .map(PlateDisciplineResponse::from)
                .sorted(
                        Comparator.comparing(PlateDisciplineResponse::eye)
                                .reversed()
                                .thenComparing(
                                        Comparator.comparing(
                                                        (PlateDisciplineResponse p) ->
                                                                p.walks() == null ? 0 : p.walks())
                                                .reversed())
                                .thenComparing(PlateDisciplineResponse::jerseyNumber))
                .toList();
    }

    public TeamSummaryResponse getTeamSummary() {
        List<PlayerStatsResponse> qualified =
                playerStatsRepository.findAll().stream()
                        .filter(this::isQualified)
                        .map(PlayerStatsResponse::from)
                        .toList();

        int totalHits =
                qualified.stream()
                        .mapToInt(player -> player.hits() == null ? 0 : player.hits())
                        .sum();
        int totalAtBats = qualified.stream().mapToInt(PlayerStatsResponse::atBats).sum();

        List<PlayerStatsResponse> sorted =
                qualified.stream()
                        .sorted(
                                Comparator.<PlayerStatsResponse, Integer>comparing(
                                                player -> player.hits() == null ? 0 : player.hits())
                                        .reversed()
                                        .thenComparing(
                                                Comparator.comparing(
                                                                PlayerStatsResponse::battingAvg)
                                                        .reversed())
                                        .thenComparing(PlayerStatsResponse::jerseyNumber))
                        .toList();

        return new TeamSummaryResponse(
                PlayerStatsResponse.battingAverage(totalHits, totalAtBats), sorted);
    }

    private boolean isQualified(PlayerStats player) {
        return player.getAtBats() != null && player.getAtBats() >= QUALIFYING_AT_BATS;
    }
}
