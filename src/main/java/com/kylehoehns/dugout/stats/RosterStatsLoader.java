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

    private PlayerStats parseLine(String line) {
        String[] fields = line.split(",", -1);
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
                Integer.parseInt(fields[13]));
    }
}
