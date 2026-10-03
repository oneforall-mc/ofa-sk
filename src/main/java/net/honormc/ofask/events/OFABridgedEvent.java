package net.honormc.ofask.events;

import net.honormc.oneforall.event.player.PlayerEvent;
import net.minestom.server.entity.Player;

/**
 * The Minestom-side carrier for an OFA player event, so skript-minestom's
 * {@code PlayerEventMarker} resolves {@code player} inside the trigger. Never dispatched
 * through Minestom's own event tree — {@link OFAEventBridge} hands the wrapper straight to
 * Skript's plugin manager, the same way the menu pack does.
 */
public class OFABridgedEvent implements net.minestom.server.event.trait.PlayerEvent {

    private final PlayerEvent ofa;
    private final Player player;

    public OFABridgedEvent(PlayerEvent ofa, Player player) {
        this.ofa = ofa;
        this.player = player;
    }

    public PlayerEvent ofa() {
        return ofa;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
