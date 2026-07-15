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

    private static final String CSV_PATH = "roster-stats.csv";

    private final PlayerStatsRepository playerStatsRepository;

    public RosterStatsLoader(PlayerStatsRepository playerStatsRepository) {
        this.playerStatsRepository = playerStatsRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (playerStatsRepository.count() == 0) {
            playerStatsRepository.saveAll(readRoster());
        }
    }

    private List<PlayerStats> readRoster() throws IOException {
        List<PlayerStats> roster = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(CSV_PATH);
        try (InputStream inputStream = resource.getInputStream();
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
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
                roster.add(parseLine(line));
            }
        }
        return roster;
    }

    private PlayerStats parseLine(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length < 14) {
            throw new IllegalStateException(
                    "Malformed roster-stats.csv row (expected 14 columns): " + line);
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
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
    }
}
