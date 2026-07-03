package com.kylehoehns.dugout.player;

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
    public void run(String... args) throws IOException {
        if (playerStatsRepository.count() != 0) {
            return;
        }

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
                PlayerStats stats = parseLine(line);
                if (stats != null) {
                    rows.add(stats);
                }
            }
        }

        if (!rows.isEmpty()) {
            playerStatsRepository.saveAll(rows);
        }
    }

    private PlayerStats parseLine(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length < 14) {
            log.warn("Skipping malformed roster stats row (wrong column count): {}", line);
            return null;
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        try {
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
                    Integer.parseInt(fields[13]));
        } catch (NumberFormatException e) {
            log.warn("Skipping malformed roster stats row (non-numeric field): {}", line);
            return null;
        }
    }
}
