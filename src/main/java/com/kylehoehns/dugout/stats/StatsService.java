package com.kylehoehns.dugout.stats;

import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StatsService {

    private final PlayerStatsRepository playerStatsRepository;

    public StatsService(PlayerStatsRepository playerStatsRepository) {
        this.playerStatsRepository = playerStatsRepository;
    }

    public List<PlayerStatsResponse> getAllStats() {
        return playerStatsRepository.findAll().stream()
                .map(PlayerStatsResponse::from)
                .sorted(Comparator.comparing(PlayerStatsResponse::battingAvg).reversed())
                .toList();
    }

    public PlayerStatsResponse getStatsByJerseyNumber(Integer jerseyNumber) {
        return playerStatsRepository
                .findById(jerseyNumber)
                .map(PlayerStatsResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
