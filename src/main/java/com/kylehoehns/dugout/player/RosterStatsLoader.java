package com.kylehoehns.dugout.player;

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

    private static final String CSV_FILE = "roster-stats.csv";

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
        ClassPathResource resource = new ClassPathResource(CSV_FILE);
        try (InputStream inputStream = resource.getInputStream();
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
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
        return new PlayerStats(
                parseInt(fields, 0),
                fields[1].trim(),
                fields[2].trim(),
                parseInt(fields, 3),
                parseInt(fields, 4),
                parseInt(fields, 5),
                parseInt(fields, 6),
                parseInt(fields, 7),
                parseInt(fields, 8),
                parseInt(fields, 9),
                parseInt(fields, 10),
                parseInt(fields, 11),
                parseInt(fields, 12),
                parseInt(fields, 13));
    }

    private static int parseInt(String[] fields, int index) {
        return Integer.parseInt(fields[index].trim());
    }
}
