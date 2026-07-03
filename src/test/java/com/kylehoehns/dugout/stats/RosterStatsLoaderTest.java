package com.kylehoehns.dugout.stats;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RosterStatsLoader}'s defensive CSV row parsing. {@code parseRow} is
 * private, so it is invoked via reflection rather than widening its visibility just for tests.
 */
class RosterStatsLoaderTest {

	@Test
	@DisplayName("skips a row when a numeric column cannot be parsed")
	void should_skip_row_when_numeric_column_is_malformed() throws Exception {
		// given: a loader instance and a CSV row with a non-numeric at-bats column
		RosterStatsLoader loader = new RosterStatsLoader(null);
		String malformedLine = "4,Tate,Hoehns,NOT_A_NUMBER,12,5,3,1,2,4,6,2,1,0";

		Method parseRow = RosterStatsLoader.class.getDeclaredMethod("parseRow", String.class);
		parseRow.setAccessible(true);

		// when
		Object result = parseRow.invoke(loader, malformedLine);

		// then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("skips a row when it has fewer columns than expected")
	void should_skip_row_when_column_count_is_too_short() throws Exception {
		// given: a loader instance and a CSV row missing trailing columns
		RosterStatsLoader loader = new RosterStatsLoader(null);
		String shortLine = "4,Tate,Hoehns,28,12";

		Method parseRow = RosterStatsLoader.class.getDeclaredMethod("parseRow", String.class);
		parseRow.setAccessible(true);

		// when
		Object result = parseRow.invoke(loader, shortLine);

		// then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("parses a well-formed row into a PlayerStats instance")
	void should_parse_row_when_all_columns_are_valid() throws Exception {
		// given: a loader instance and a well-formed CSV row
		RosterStatsLoader loader = new RosterStatsLoader(null);
		String validLine = "4,Tate,Hoehns,28,12,5,3,1,2,4,6,2,1,0";

		Method parseRow = RosterStatsLoader.class.getDeclaredMethod("parseRow", String.class);
		parseRow.setAccessible(true);

		// when
		Object result = parseRow.invoke(loader, validLine);

		// then
		assertThat(result).isInstanceOf(PlayerStats.class);
		PlayerStats playerStats = (PlayerStats) result;
		assertThat(playerStats.getFirstName()).isEqualTo("Tate");
		assertThat(playerStats.getLastName()).isEqualTo("Hoehns");
		assertThat(playerStats.getGamesPlayed()).isEqualTo(28);
		assertThat(playerStats.getAtBats()).isEqualTo(12);
	}
}
