package com.kylehoehns.dugout.stats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlayerStatsService {

    private static final int MIN_ELIGIBLE_AT_BATS = 10;

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

    public TeamStatsResponse getTeamStats() {
        List<PlayerStatsResponse> eligible =
                playerStatsRepository.findAll().stream()
                        .map(PlayerStatsResponse::from)
                        .filter(
                                player ->
                                        player.atBats() != null
                                                && player.atBats() >= MIN_ELIGIBLE_AT_BATS)
                        .toList();

        if (eligible.isEmpty()) {
            return TeamStatsResponse.from(List.of());
        }

        // max() with jerseyNumber comparator reversed() picks the LOWEST jerseyNumber as the
        // final tie-break
        PlayerStatsResponse hitsLeader =
                eligible.stream()
                        .max(
                                Comparator.comparingInt(
                                                (PlayerStatsResponse p) ->
                                                        p.hits() == null ? 0 : p.hits())
                                        .thenComparing(PlayerStatsResponse::battingAvg)
                                        .thenComparing(
                                                Comparator.comparing(
                                                                PlayerStatsResponse::jerseyNumber)
                                                        .reversed()))
                        .orElseThrow();

        List<PlayerStatsResponse> rest =
                eligible.stream()
                        .filter(player -> !player.jerseyNumber().equals(hitsLeader.jerseyNumber()))
                        .sorted(
                                Comparator.comparing(PlayerStatsResponse::battingAvg)
                                        .reversed()
                                        .thenComparing(PlayerStatsResponse::jerseyNumber))
                        .toList();

        List<PlayerStatsResponse> ordered = new ArrayList<>();
        ordered.add(hitsLeader);
        ordered.addAll(rest);

        return TeamStatsResponse.from(ordered);
    }
}
