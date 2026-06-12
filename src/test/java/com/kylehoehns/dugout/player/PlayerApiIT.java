package com.kylehoehns.dugout.player;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PlayerApiIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	PlayerRepository playerRepository;

	@BeforeEach
	void setUp() {
		playerRepository.deleteAll();
	}

	@Test
	@DisplayName("returns all players when listing")
	void should_return_all_players_when_listing() throws Exception {
		// given
		playerRepository.saveAll(List.of(
			new Player(1L, "Willie Mays", "Center Field"),
			new Player(2L, "Mickey Mantle", "Center Field")
		));

		// when
		var response = mockMvc.perform(get("/api/players"))
			.andReturn().getResponse();
		var players = objectMapper.readValue(response.getContentAsString(), Player[].class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(players).hasSize(2);
		assertThat(players).extracting(Player::getName)
			.containsExactlyInAnyOrder("Willie Mays", "Mickey Mantle");
	}

	@Test
	@DisplayName("returns the player when the id exists")
	void should_return_single_player_when_id_exists() throws Exception {
		// given
		var player = new Player(1L, "Willie Mays", "Center Field");
		playerRepository.save(player);

		// when
		var response = mockMvc.perform(get("/api/players/{id}", 1L))
			.andReturn().getResponse();
		var result = objectMapper.readValue(response.getContentAsString(), Player.class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Willie Mays");
		assertThat(result.getPosition()).isEqualTo("Center Field");
	}

	@Test
	@DisplayName("returns 404 when the player id is unknown")
	void should_return_404_when_player_is_missing() throws Exception {
		// given
		long unknownId = 999L;

		// when
		var status = mockMvc.perform(get("/api/players/{id}", unknownId))
			.andReturn().getResponse().getStatus();

		// then
		assertThat(status).isEqualTo(404);
	}

	private void seedRoster() {
		playerRepository.saveAll(List.of(
			new Player(1L, "Hank Aaron", "RF"),
			new Player(2L, "Willie Mays", "CF"),
			new Player(3L, "Ozzie Smith", "SS")
		));
	}

	@Test
	@DisplayName("returns all players when no position filter is provided")
	void should_return_all_players_when_no_position_filter_is_provided() throws Exception {
		// given
		seedRoster();

		// when
		var response = mockMvc.perform(get("/api/players"))
			.andReturn().getResponse();
		var players = objectMapper.readValue(response.getContentAsString(), Player[].class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(players).hasSize(3);
		assertThat(players).extracting(Player::getName)
			.containsExactlyInAnyOrder("Hank Aaron", "Willie Mays", "Ozzie Smith");
	}

	@Test
	@DisplayName("returns only the matching player when filtering by position (uppercase)")
	void should_return_matching_player_when_filtering_by_position_uppercase() throws Exception {
		// given
		seedRoster();

		// when
		var response = mockMvc.perform(get("/api/players").param("position", "SS"))
			.andReturn().getResponse();
		var players = objectMapper.readValue(response.getContentAsString(), Player[].class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(players).hasSize(1);
		assertThat(players[0].getName()).isEqualTo("Ozzie Smith");
		assertThat(players[0].getPosition()).isEqualTo("SS");
	}

	@Test
	@DisplayName("returns only the matching player when filtering by position (lowercase, case-insensitive)")
	void should_return_matching_player_when_filtering_by_position_lowercase() throws Exception {
		// given
		seedRoster();

		// when
		var response = mockMvc.perform(get("/api/players").param("position", "ss"))
			.andReturn().getResponse();
		var players = objectMapper.readValue(response.getContentAsString(), Player[].class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(players).hasSize(1);
		assertThat(players[0].getName()).isEqualTo("Ozzie Smith");
		assertThat(players[0].getPosition()).isEqualTo("SS");
	}

	@Test
	@DisplayName("returns empty list when no players match the given position")
	void should_return_empty_list_when_no_players_match_position() throws Exception {
		// given
		seedRoster();

		// when
		var response = mockMvc.perform(get("/api/players").param("position", "ZZ"))
			.andReturn().getResponse();
		var players = objectMapper.readValue(response.getContentAsString(), Player[].class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(players).isEmpty();
	}

	@Test
	@DisplayName("returns all players when position param is an empty string")
	void should_return_all_players_when_position_param_is_empty_string() throws Exception {
		// given
		seedRoster();

		// when
		var response = mockMvc.perform(get("/api/players").param("position", ""))
			.andReturn().getResponse();
		var players = objectMapper.readValue(response.getContentAsString(), Player[].class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(players).hasSize(3);
		assertThat(players).extracting(Player::getName)
			.containsExactlyInAnyOrder("Hank Aaron", "Willie Mays", "Ozzie Smith");
	}

	@Test
	@DisplayName("returns all players when position param is whitespace only")
	void should_return_all_players_when_position_param_is_whitespace_only() throws Exception {
		// given
		seedRoster();

		// when
		var response = mockMvc.perform(get("/api/players").param("position", "  "))
			.andReturn().getResponse();
		var players = objectMapper.readValue(response.getContentAsString(), Player[].class);

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(players).hasSize(3);
		assertThat(players).extracting(Player::getName)
			.containsExactlyInAnyOrder("Hank Aaron", "Willie Mays", "Ozzie Smith");
	}

	@Test
	@DisplayName("creates a player and returns it with 201 status")
	void should_create_player_and_return_201_when_posting_valid_request() throws Exception {
		// given
		var request = new CreatePlayerRequest("Babe Ruth", "Outfield");
		var requestBody = objectMapper.writeValueAsString(request);

		// when
		var response = mockMvc.perform(post("/api/players")
			.contentType("application/json")
			.content(requestBody))
			.andReturn().getResponse();
		var result = objectMapper.readValue(response.getContentAsString(), Player.class);

		// then
		assertThat(response.getStatus()).isEqualTo(201);
		assertThat(result.getName()).isEqualTo("Babe Ruth");
		assertThat(result.getPosition()).isEqualTo("Outfield");
		assertThat(result.getId()).isNotNull();

		// verify it persisted
		var saved = playerRepository.findById(result.getId());
		assertThat(saved).isPresent();
		assertThat(saved.get().getName()).isEqualTo("Babe Ruth");
	}
}
