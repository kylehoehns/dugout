package com.kylehoehns.dugout.stats;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class PlayerStatsController {

    private final PlayerStatsService playerStatsService;

    public PlayerStatsController(PlayerStatsService playerStatsService) {
        this.playerStatsService = playerStatsService;
    }

    @GetMapping
    public List<PlayerStatsResponse> getAllPlayerStats() {
        return playerStatsService.getAllPlayerStats();
    }

    @GetMapping("/team")
    public TeamStatsResponse getTeamStats() {
        return playerStatsService.getTeamStats();
    }

    @GetMapping("/{number}")
    public PlayerStatsResponse getPlayerStatsByJerseyNumber(@PathVariable Integer number) {
        return playerStatsService.getPlayerStatsByJerseyNumber(number);
    }
}
