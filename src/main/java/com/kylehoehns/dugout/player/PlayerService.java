package com.kylehoehns.dugout.player;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlayerService {

    private static final List<Player> PLAYERS = List.of(
        new Player(1L, "Hank Aaron", "RF"),
        new Player(2L, "Willie Mays", "CF"),
        new Player(3L, "Ozzie Smith", "SS")
    );

    public List<Player> getPlayers() {
        return PLAYERS;
    }

    public Player getPlayerById(Long id) {
        return PLAYERS
            .stream()
            .filter(player -> player.id().equals(id))
            .findFirst()
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND)
            );
    }
}
