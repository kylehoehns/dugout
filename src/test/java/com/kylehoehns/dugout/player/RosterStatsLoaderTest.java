package com.kylehoehns.dugout.player;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RosterStatsLoaderTest {

    private final RosterStatsLoader loader = new RosterStatsLoader(null);

    @Test
    @DisplayName("parses a well-formed CSV row into player stats")
    void should_parse_row_when_well_formed() throws Exception {
        // given
        String line = "4,Tate,Hoehns,16,28,12,1,0,0,10,17,9,8,20";

        // when
        PlayerStats stats = parseLine(line);

        // then
        assertThat(stats).isNotNull();
        assertThat(stats.getJerseyNumber()).isEqualTo(4);
        assertThat(stats.getFirstName()).isEqualTo("Tate");
        assertThat(stats.getLastName()).isEqualTo("Hoehns");
        assertThat(stats.getAtBats()).isEqualTo(28);
        assertThat(stats.getHits()).isEqualTo(12);
        assertThat(stats.getStolenBases()).isEqualTo(20);
    }

    @Test
    @DisplayName("skips a row when it has the wrong column count")
    void should_skip_row_when_column_count_is_wrong() throws Exception {
        // given
        String line = "4,Tate,Hoehns,16,28,12,1,0,0,10,17,9,8";

        // when
        PlayerStats stats = parseLine(line);

        // then
        assertThat(stats).isNull();
    }

    @Test
    @DisplayName("skips a row when it has too many columns")
    void should_skip_row_when_column_count_has_extra_fields() throws Exception {
        // given
        String line = "4,Tate,Hoehns,16,28,12,1,0,0,10,17,9,8,20,99";

        // when
        PlayerStats stats = parseLine(line);

        // then
        assertThat(stats).isNull();
    }

    @Test
    @DisplayName("skips a row when a numeric field is non-numeric")
    void should_skip_row_when_field_is_non_numeric() throws Exception {
        // given
        String line = "abc,Tate,Hoehns,16,28,12,1,0,0,10,17,9,8,20";

        // when
        PlayerStats stats = parseLine(line);

        // then
        assertThat(stats).isNull();
    }

    private PlayerStats parseLine(String line) throws Exception {
        Method parseLine = RosterStatsLoader.class.getDeclaredMethod("parseLine", String.class);
        parseLine.setAccessible(true);
        return (PlayerStats) parseLine.invoke(loader, line);
    }
}
