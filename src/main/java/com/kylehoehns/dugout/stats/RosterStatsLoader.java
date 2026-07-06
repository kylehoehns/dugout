package com.kylehoehns.dugout.stats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class RosterStatsLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RosterStatsLoader.class);

    private static final String CSV_FILE = "roster-stats.csv";

    private final PlayerStatsRepository playerStatsRepository;

    public RosterStatsLoader(PlayerStatsRepository playerStatsRepository) {
        this.playerStatsRepository = playerStatsRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (playerStatsRepository.count() == 0) {
            playerStatsRepository.saveAll(readRosterStats());
        }
    }

    private List<PlayerStats> readRosterStats() throws IOException {
        List<PlayerStats> rosterStats = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(CSV_FILE);
        try (InputStream inputStream = resource.getInputStream();
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                PlayerStats playerStats = parseLine(line);
                if (playerStats != null) {
                    rosterStats.add(playerStats);
                }
            }
        }
        return rosterStats;
    }

    PlayerStats parseLine(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length < 14) {
            log.warn("Skipping malformed roster-stats row (too few columns): {}", line);
            return null;
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        try {
            return new PlayerStats(
                    Integer.valueOf(fields[0]),
                    fields[1],
                    fields[2],
                    Integer.valueOf(fields[3]),
                    Integer.valueOf(fields[4]),
                    Integer.valueOf(fields[5]),
                    Integer.valueOf(fields[6]),
                    Integer.valueOf(fields[7]),
                    Integer.valueOf(fields[8]),
                    Integer.valueOf(fields[9]),
                    Integer.valueOf(fields[10]),
                    Integer.valueOf(fields[11]),
                    Integer.valueOf(fields[12]),
                    Integer.valueOf(fields[13]));
        } catch (NumberFormatException e) {
            log.warn("Skipping roster-stats row with invalid numeric value: {}", line, e);
            return null;
        }
    }
}
