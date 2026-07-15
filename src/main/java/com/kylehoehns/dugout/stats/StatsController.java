package com.kylehoehns.dugout.stats;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public List<PlayerStatsResponse> getAllStats() {
        return statsService.getAllStats();
    }

    @GetMapping("/{number}")
    public PlayerStatsResponse getStatsByJerseyNumber(@PathVariable Integer number) {
        return statsService.getStatsByJerseyNumber(number);
    }
}
