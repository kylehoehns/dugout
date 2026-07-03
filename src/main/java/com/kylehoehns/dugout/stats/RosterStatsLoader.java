package com.kylehoehns.dugout.stats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class RosterStatsLoader implements CommandLineRunner {

    private static final String CSV_FILE = "roster-stats.csv";

    private final PlayerStatsRepository playerStatsRepository;

    public RosterStatsLoader(PlayerStatsRepository playerStatsRepository) {
        this.playerStatsRepository = playerStatsRepository;
    }

    @Override
    public void run(String... args) throws IOException {
        if (playerStatsRepository.count() == 0) {
            playerStatsRepository.saveAll(loadFromCsv());
        }
    }

    private List<PlayerStats> loadFromCsv() throws IOException {
        List<PlayerStats> stats = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(CSV_FILE);
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            boolean firstLine = true;
            String line;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                PlayerStats parsed = parseLine(line);
                if (parsed != null) {
                    stats.add(parsed);
                }
            }
        }
        return stats;
    }

    private PlayerStats parseLine(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length < 14) {
            return null;
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        return new PlayerStats(
            Integer.parseInt(fields[0]),
            fields[1],
            fields[2],
            Integer.parseInt(fields[3]),
            Integer.parseInt(fields[4]),
            Integer.parseInt(fields[5]),
            Integer.parseInt(fields[6]),
            Integer.parseInt(fields[7]),
            Integer.parseInt(fields[8]),
            Integer.parseInt(fields[9]),
            Integer.parseInt(fields[10]),
            Integer.parseInt(fields[11]),
            Integer.parseInt(fields[12]),
            Integer.parseInt(fields[13])
        );
    }
}
