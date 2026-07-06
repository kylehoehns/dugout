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

    public List<PlayerStatsResponse> getAllStats() {
        return playerStatsRepository.findAll().stream()
                .map(PlayerStatsResponse::from)
                .sorted(Comparator.comparingDouble(PlayerStatsResponse::battingAvg).reversed())
                .toList();
    }

    public PlayerStatsResponse getStatsByNumber(Integer number) {
        return playerStatsRepository
                .findById(number)
                .map(PlayerStatsResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
