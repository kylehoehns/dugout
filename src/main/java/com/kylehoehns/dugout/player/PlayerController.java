package com.kylehoehns.dugout.player;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @GetMapping
    public List<Player> getPlayers() {
        return List.of(
            new Player(1L, "Hank Aaron", "RF"),
            new Player(2L, "Willie Mays", "CF"),
            new Player(3L, "Ozzie Smith", "SS")
        );
    }

    @GetMapping("/{id}")
    public Player getPlayerById(@PathVariable Long id) {
        return getPlayers()
            .stream()
            .filter(player -> player.id().equals(id))
            .findFirst()
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND)
            );
    }
}
