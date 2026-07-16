package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RosterStatsLoaderTest {

    @Test
    @DisplayName("does not reload the roster when the table already has rows")
    void should_skip_seeding_when_table_is_not_empty() throws Exception {
        // given
        var playerStatsRepository = mock(PlayerStatsRepository.class);
        when(playerStatsRepository.count()).thenReturn(12L);
        var loader = new RosterStatsLoader(playerStatsRepository);

        // when
        loader.run();

        // then
        verify(playerStatsRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("parses a well-formed row into player stats")
    void should_parse_row_when_all_fields_are_present_and_numeric() throws Exception {
        // given
        var loader = new RosterStatsLoader(mock(PlayerStatsRepository.class));
        String row = "23,Mason,Reed,23,41,25,10,0,0,20,25,12,1,13";

        // when
        Optional<PlayerStats> result = invokeParseRow(loader, row);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getJerseyNumber()).isEqualTo(23);
        assertThat(result.get().getFirstName()).isEqualTo("Mason");
        assertThat(result.get().getLastName()).isEqualTo("Reed");
        assertThat(result.get().getAtBats()).isEqualTo(41);
        assertThat(result.get().getHits()).isEqualTo(25);
        assertThat(result.get().getStolenBases()).isEqualTo(13);
    }

    @Test
    @DisplayName("skips a row when it has fewer fields than expected")
    void should_skip_row_when_field_count_is_too_low() throws Exception {
        // given
        var loader = new RosterStatsLoader(mock(PlayerStatsRepository.class));
        String truncatedRow = "23,Mason,Reed,23,41,25,10,0,0,20,25,12,1";

        // when
        Optional<PlayerStats> result = invokeParseRow(loader, truncatedRow);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("skips a row when a numeric field cannot be parsed")
    void should_skip_row_when_field_is_non_numeric() throws Exception {
        // given
        var loader = new RosterStatsLoader(mock(PlayerStatsRepository.class));
        String nonNumericRow = "23,Mason,Reed,twenty-three,41,25,10,0,0,20,25,12,1,13";

        // when
        Optional<PlayerStats> result = invokeParseRow(loader, nonNumericRow);

        // then
        assertThat(result).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Optional<PlayerStats> invokeParseRow(RosterStatsLoader loader, String line)
            throws Exception {
        Method parseRow = RosterStatsLoader.class.getDeclaredMethod("parseRow", String.class);
        parseRow.setAccessible(true);
        return (Optional<PlayerStats>) parseRow.invoke(loader, line);
    }
}
