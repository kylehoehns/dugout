package com.kylehoehns.dugout.player;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
