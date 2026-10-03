package net.honormc.ofask.player;

import net.honormc.oneforall.OneForAll;
import net.honormc.oneforall.player.OFAPlayer;
import net.minestom.server.entity.Player;
import org.jspecify.annotations.Nullable;

/** Skript hands us Minestom players; OFA's APIs take {@link OFAPlayer}s. */
final class OFAPlayers {

    private OFAPlayers() {
    }

    static @Nullable OFAPlayer of(Player player) {
        return OneForAll.players().get(player.getUuid());
    }
}
