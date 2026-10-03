package net.honormc.ofask.inventory;

import net.honormc.oneforall.inventory.OFAInventory;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;

/**
 * Fired via {@link net.minestom.server.event.EventDispatcher} when the last (or only)
 * viewer closes an {@link OFAInventory} created through this addon's Skript syntax.
 * {@link OFAMenuCloseWrapper} exposes it to Skript scripts as {@code on ofa menu close}.
 */
public class OFAMenuCloseEvent implements PlayerEvent {

    private final OFAInventory menu;
    private final Player player;

    public OFAMenuCloseEvent(OFAInventory menu, Player player) {
        this.menu = menu;
        this.player = player;
    }

    public OFAInventory menu() {
        return menu;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
