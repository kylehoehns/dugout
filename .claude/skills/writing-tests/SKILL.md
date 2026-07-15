---
name: writing-tests
description: >
  Use when writing, adding, or improving tests for this project — asks like
  "write tests for the players API", "add a test for the 404 case", or "cover
  the controller with tests". Encodes the house integration-testing conventions
  so generated tests match the rest of the suite.
---

# Writing Tests

Tests in this project are **full-context integration tests driven through
`MockMvc`** — they boot the real Spring context against the in-memory H2
database and call endpoints through `MockMvc`.

## Setup — use exactly this, do not substitute

- Annotate the class with `@SpringBootTest` (leave the default MOCK web
  environment — do **NOT** set `webEnvironment = RANDOM_PORT` or `DEFINED_PORT`)
  and `@AutoConfigureMockMvc`.
- `@Autowired MockMvc` to call endpoints; `@Autowired ObjectMapper` to read
  JSON response bodies.
- Do **NOT** use `RestTemplate`, `TestRestTemplate`, `WebTestClient`,
  `@WebMvcTest`, or `@MockBean`/mocked services. We exercise the real wiring,
  not a slice.

## Conventions — all required

1. **Class:** name it `<Area>IT` (the `IT` suffix marks an integration test),
   e.g. `PlayerApiIT`.
2. **Method names:** strict snake_case `should_<outcome>_when_<condition>`,
   e.g. `should_return_single_player_when_id_exists`.
3. **@DisplayName:** every test carries a full-sentence `@DisplayName`,
   e.g. `@DisplayName("returns the player when the id exists")`.
4. **Body:** structure every test with `// given`, `// when`, `// then` blocks,
   in that order.
5. **Assertions: AssertJ only** (`assertThat(...)`). Do **NOT** use JUnit
   `assertEquals` / `assertTrue` / `assertThrows`, and do **NOT** use MockMvc's
   `.andExpect(...)` matchers. Instead: perform the request, capture the
   response, and assert on it with AssertJ.

## Example

```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PlayerApiIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("returns the player when the id exists")
    void should_return_single_player_when_id_exists() throws Exception {
        // given
        long playerId = 2L;

        // when
        var response = mockMvc.perform(get("/api/players/{id}", playerId))
            .andReturn().getResponse();
        var player = objectMapper.readValue(response.getContentAsString(), Player.class);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(player.getName()).isEqualTo("Willie Mays");
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
}
```

## This project's stack (Spring Boot 4 / Jackson 3) — use these exact imports

These packages moved recently; model training from before 2025 usually gets them
wrong, so copy these paths exactly:

- `import tools.jackson.databind.ObjectMapper;`
  — Jackson 3 moved `databind` from `com.fasterxml.jackson` to `tools.jackson`.
- `import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;`
  — Spring Boot 4 moved it out of `org.springframework.boot.test.autoconfigure.web.servlet`.
- `import org.springframework.test.web.servlet.MockMvc;`
- `import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;`
- `import static org.assertj.core.api.Assertions.assertThat;`

**Test dependency:** the project uses `spring-boot-starter-webmvc-test`, which
already provides both MockMvc autoconfiguration and AssertJ. Do **NOT** switch it
to `spring-boot-starter-test` — that starter does not include MockMvc support in
Boot 4.
