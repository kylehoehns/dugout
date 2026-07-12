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
                roster.add(parseLine(line));
            }
        }
        return roster;
    }

    private static final int EXPECTED_COLUMN_COUNT = 14;

    private PlayerStats parseLine(String line) {
        String[] fields = line.split(",", -1);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        if (fields.length != EXPECTED_COLUMN_COUNT) {
            throw new IllegalStateException(
                    CSV_PATH
                            + ": expected "
                            + EXPECTED_COLUMN_COUNT
                            + " columns but found "
                            + fields.length
                            + " in row: "
                            + line);
        }
        return new PlayerStats(
                parseInt(fields[0], "jerseyNumber", line),
                fields[1],
                fields[2],
                parseInt(fields[3], "gamesPlayed", line),
                parseInt(fields[4], "atBats", line),
                parseInt(fields[5], "hits", line),
                parseInt(fields[6], "doubles", line),
                parseInt(fields[7], "triples", line),
                parseInt(fields[8], "homeRuns", line),
                parseInt(fields[9], "rbi", line),
                parseInt(fields[10], "runs", line),
                parseInt(fields[11], "walks", line),
                parseInt(fields[12], "strikeouts", line),
                parseInt(fields[13], "stolenBases", line));
    }

    private int parseInt(String value, String fieldName, String line) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    CSV_PATH
                            + ": could not parse "
                            + fieldName
                            + " (value: '"
                            + value
                            + "') in row: "
                            + line,
                    e);
        }
    }
}
