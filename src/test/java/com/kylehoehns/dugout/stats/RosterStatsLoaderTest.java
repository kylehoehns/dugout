package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RosterStatsLoaderTest {

    private final RosterStatsLoader rosterStatsLoader = new RosterStatsLoader(null);

    @Test
    @DisplayName("skips a row with too few columns instead of throwing")
    void should_skip_row_when_column_count_is_too_short() throws Exception {
        // given
        String shortRow = "4,Tate,Hoehns,16,28,12,1,0,0,10,17,9,8";

        // when
        PlayerStats result = invokeParseLine(shortRow);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("skips a row with a non-numeric field instead of throwing")
    void should_skip_row_when_numeric_field_is_invalid() throws Exception {
        // given
        String invalidNumericRow = "4,Tate,Hoehns,16,28,twelve,1,0,0,10,17,9,8,20";

        // when
        PlayerStats result = invokeParseLine(invalidNumericRow);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("parses a well-formed row into player stats")
    void should_parse_row_when_row_is_well_formed() throws Exception {
        // given
        String validRow = "4,Tate,Hoehns,16,28,12,1,0,0,10,17,9,8,20";

        // when
        PlayerStats result = invokeParseLine(validRow);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getJerseyNumber()).isEqualTo(4);
        assertThat(result.getFirstName()).isEqualTo("Tate");
        assertThat(result.getLastName()).isEqualTo("Hoehns");
    }

    private PlayerStats invokeParseLine(String line) throws Exception {
        Method parseLine = RosterStatsLoader.class.getDeclaredMethod("parseLine", String.class);
        parseLine.setAccessible(true);
        return (PlayerStats) parseLine.invoke(rosterStatsLoader, line);
    }
}
