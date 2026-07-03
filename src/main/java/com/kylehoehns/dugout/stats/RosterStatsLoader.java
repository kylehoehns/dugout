package com.kylehoehns.dugout.stats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class RosterStatsLoader implements CommandLineRunner {

    private final PlayerStatsRepository playerStatsRepository;

    public RosterStatsLoader(PlayerStatsRepository playerStatsRepository) {
        this.playerStatsRepository = playerStatsRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (playerStatsRepository.count() == 0) {
            playerStatsRepository.saveAll(loadFromCsv());
        }
    }

    private List<PlayerStats> loadFromCsv() throws IOException {
        List<PlayerStats> rows = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("roster-stats.csv");
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                rows.add(parseLine(line));
            }
        }
        return rows;
    }

    private PlayerStats parseLine(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length != 14) {
            throw new IllegalStateException(
                "Malformed roster-stats.csv row (expected 14 fields, got " + fields.length + "): " + line);
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
