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
    public void run(String... args) throws IOException {
        if (playerStatsRepository.count() == 0) {
            List<PlayerStats> parsed = parseCsv();
            playerStatsRepository.saveAll(parsed);
        }
    }

    private List<PlayerStats> parseCsv() throws IOException {
        List<PlayerStats> result = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(CSV_PATH);
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader =
                 new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
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
                PlayerStats playerStats = parseRow(line);
                if (playerStats != null) {
                    result.add(playerStats);
                }
            }
        }
        return result;
    }

    private PlayerStats parseRow(String line) {
        String[] columns = line.split(",", -1);
        if (columns.length < 14) {
            return null;
        }
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
        }
        try {
            return new PlayerStats(
                Integer.valueOf(columns[0]),
                columns[1],
                columns[2],
                Integer.valueOf(columns[3]),
                Integer.valueOf(columns[4]),
                Integer.valueOf(columns[5]),
                Integer.valueOf(columns[6]),
                Integer.valueOf(columns[7]),
                Integer.valueOf(columns[8]),
                Integer.valueOf(columns[9]),
                Integer.valueOf(columns[10]),
                Integer.valueOf(columns[11]),
                Integer.valueOf(columns[12]),
                Integer.valueOf(columns[13])
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
