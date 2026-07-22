package com.kylehoehns.dugout.stats;

import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlayerStatsService {

    private static final int MINIMUM_QUALIFYING_PLATE_APPEARANCES = 10;

    // Highest eye first; ties broken by more walks, then by jersey number ascending
    // for a deterministic order.
    private static final Comparator<PlateDisciplineResponse> PLATE_DISCIPLINE_ORDER =
            Comparator.comparing(PlateDisciplineResponse::eye, Comparator.reverseOrder())
                    .thenComparing(PlateDisciplineResponse::walks, Comparator.reverseOrder())
                    .thenComparing(PlateDisciplineResponse::jerseyNumber);

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

    public List<PlateDisciplineResponse> getPlateDiscipline() {
        return playerStatsRepository.findAll().stream()
                .filter(PlayerStatsService::isQualifyingForPlateDiscipline)
                .map(PlateDisciplineResponse::from)
                .sorted(PLATE_DISCIPLINE_ORDER)
                .toList();
    }

    private static boolean isQualifyingForPlateDiscipline(PlayerStats stats) {
        int plateAppearances =
                PlateDisciplineResponse.plateAppearances(stats.getAtBats(), stats.getWalks());
        return plateAppearances >= MINIMUM_QUALIFYING_PLATE_APPEARANCES;
    }
}
