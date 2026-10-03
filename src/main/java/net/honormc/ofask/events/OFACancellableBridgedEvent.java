package net.honormc.ofask.events;

import net.honormc.oneforall.event.player.CancellablePlayerEvent;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;

/**
 * {@link OFABridgedEvent} for a cancellable OFA event. Cancellation writes straight through
 * to the OFA event, so Skript's own {@code cancel event} reaches the Java listeners that run
 * after the script.
 */
public class OFACancellableBridgedEvent extends OFABridgedEvent implements CancellableEvent {

    public OFACancellableBridgedEvent(CancellablePlayerEvent ofa, Player player) {
        super(ofa, player);
    }

    @Override
    public CancellablePlayerEvent ofa() {
        return (CancellablePlayerEvent) super.ofa();
    }

    @Override
    public boolean isCancelled() {
        return ofa().cancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        ofa().cancelled(cancel);
    }
}
