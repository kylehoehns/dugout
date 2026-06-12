package com.kylehoehns.dugout.player;

import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @PostConstruct
    public void seedPlayers() {
        if (playerRepository.count() == 0) {
            playerRepository.saveAll(List.of(
                new Player(1L, "Hank Aaron", "RF"),
                new Player(2L, "Willie Mays", "CF"),
                new Player(3L, "Ozzie Smith", "SS")
            ));
        }
    }

    public List<Player> getPlayers(String position) {
        if (position == null || position.isBlank()) {
            return playerRepository.findAll();
        }
        return playerRepository.findByPositionIgnoreCase(position.strip());
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND)
            );
    }

    public Player createPlayer(CreatePlayerRequest request) {
        long nextId = playerRepository.findAll().stream()
            .mapToLong(Player::getId)
            .max()
            .orElse(0L) + 1;
        Player player = new Player(nextId, request.name(), request.position());
        return playerRepository.save(player);
    }
}
