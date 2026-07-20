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

    private static final String CSV_PATH = "roster-stats.csv";

    // Column,Header per roster-stats.csv:
    // Number,First,Last,GP,AB,H,2B,3B,HR,RBI,R,BB,SO,SB
    private static final int COL_NUMBER = 0;
    private static final int COL_FIRST = 1;
    private static final int COL_LAST = 2;
    private static final int COL_GP = 3;
    private static final int COL_AB = 4;
    private static final int COL_H = 5;
    private static final int COL_DOUBLES = 6;
    private static final int COL_TRIPLES = 7;
    private static final int COL_HR = 8;
    private static final int COL_RBI = 9;
    private static final int COL_RUNS = 10;
    private static final int COL_BB = 11;
    private static final int COL_SO = 12;
    private static final int COL_SB = 13;
    private static final int EXPECTED_COLUMN_COUNT = 14;

    private final PlayerStatsRepository playerStatsRepository;

    public RosterStatsLoader(PlayerStatsRepository playerStatsRepository) {
        this.playerStatsRepository = playerStatsRepository;
    }

    @Override
    public void run(String... args) throws IOException {
        if (playerStatsRepository.count() > 0) {
            return;
        }
        List<PlayerStats> parsed = parseCsv();
        if (!parsed.isEmpty()) {
            playerStatsRepository.saveAll(parsed);
        }
    }

    private List<PlayerStats> parseCsv() throws IOException {
        List<PlayerStats> stats = new ArrayList<>();
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
                PlayerStats playerStats = parseLine(line);
                if (playerStats != null) {
                    stats.add(playerStats);
                }
            }
        }
        return stats;
    }

    private PlayerStats parseLine(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length != EXPECTED_COLUMN_COUNT) {
            log.warn(
                    "Skipping malformed roster-stats.csv row: expected {} columns but found {} in"
                            + " line \"{}\"",
                    EXPECTED_COLUMN_COUNT,
                    fields.length,
                    line);
            return null;
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        try {
            return new PlayerStats(
                    Integer.parseInt(fields[COL_NUMBER]),
                    fields[COL_FIRST],
                    fields[COL_LAST],
                    Integer.parseInt(fields[COL_GP]),
                    Integer.parseInt(fields[COL_AB]),
                    Integer.parseInt(fields[COL_H]),
                    Integer.parseInt(fields[COL_DOUBLES]),
                    Integer.parseInt(fields[COL_TRIPLES]),
                    Integer.parseInt(fields[COL_HR]),
                    Integer.parseInt(fields[COL_RBI]),
                    Integer.parseInt(fields[COL_RUNS]),
                    Integer.parseInt(fields[COL_BB]),
                    Integer.parseInt(fields[COL_SO]),
                    Integer.parseInt(fields[COL_SB]));
        } catch (NumberFormatException e) {
            log.warn(
                    "Skipping malformed roster-stats.csv row: could not parse numeric column in"
                            + " line \"{}\" ({})",
                    line,
                    e.getMessage());
            return null;
        }
    }
}
