package com.kylehoehns.dugout.player;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PlayerApiIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("returns all players")
    void should_return_all_players() throws Exception {
        // when
        var response = mockMvc.perform(get("/api/players"))
            .andReturn().getResponse();
        var players = objectMapper.readValue(
            response.getContentAsString(),
            new TypeReference<List<Player>>() {}
        );

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(players).hasSize(3);
        assertThat(players.get(0).getName()).isEqualTo("Hank Aaron");
        assertThat(players.get(1).getName()).isEqualTo("Willie Mays");
        assertThat(players.get(2).getName()).isEqualTo("Ozzie Smith");
    }

    @Test
    @DisplayName("returns a single player when the id exists")
    void should_return_single_player_when_id_exists() throws Exception {
        // given
        long playerId = 2L;

        // when
        var response = mockMvc.perform(get("/api/players/{id}", playerId))
            .andReturn().getResponse();
        var player = objectMapper.readValue(response.getContentAsString(), Player.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(player.getId()).isEqualTo(2L);
        assertThat(player.getName()).isEqualTo("Willie Mays");
        assertThat(player.getPosition()).isEqualTo("CF");
    }

    @Test
    @DisplayName("returns 404 when the player id does not exist")
    void should_return_404_when_player_is_missing() throws Exception {
        // given
        long unknownId = 999L;

        // when
        var status = mockMvc.perform(get("/api/players/{id}", unknownId))
            .andReturn().getResponse().getStatus();

        // then
        assertThat(status).isEqualTo(404);
    }

    @Test
    @DisplayName("creates a new player and returns 201 CREATED")
    void should_create_player_and_return_created_status() throws Exception {
        // given
        var request = new CreatePlayerRequest("Babe Ruth", "LF");
        var requestJson = objectMapper.writeValueAsString(request);

        // when
        var response = mockMvc.perform(post("/api/players")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
            .andReturn().getResponse();
        var player = objectMapper.readValue(response.getContentAsString(), Player.class);

        // then
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(player.getId()).isEqualTo(4L);
        assertThat(player.getName()).isEqualTo("Babe Ruth");
        assertThat(player.getPosition()).isEqualTo("LF");
    }
}
