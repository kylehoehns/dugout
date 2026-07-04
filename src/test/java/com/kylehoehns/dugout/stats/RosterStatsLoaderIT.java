package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RosterStatsLoaderIT {

    @Autowired PlayerStatsRepository playerStatsRepository;

    @Autowired RosterStatsLoader rosterStatsLoader;

    @Test
    @DisplayName("does not reload the roster when the table already has rows")
    void should_not_reload_roster_when_table_already_has_rows() throws Exception {
        // given
        playerStatsRepository.deleteAll();
        playerStatsRepository.save(
                new PlayerStats(1, "Existing", "Player", 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0));
        long countBefore = playerStatsRepository.count();

        // when
        rosterStatsLoader.run();

        // then
        assertThat(playerStatsRepository.count()).isEqualTo(countBefore);
    }
}
