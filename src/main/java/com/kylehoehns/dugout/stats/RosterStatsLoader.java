package com.kylehoehns.dugout.stats;

import java.io.BufferedReader;
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
        if (playerStatsRepository.count() != 0) {
            return;
        }

        List<PlayerStats> rows = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("roster-stats.csv");
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
            return null;
        }
    }
}
