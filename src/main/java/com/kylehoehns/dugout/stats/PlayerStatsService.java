package com.kylehoehns.dugout.stats;

import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlayerStatsService {

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
}
