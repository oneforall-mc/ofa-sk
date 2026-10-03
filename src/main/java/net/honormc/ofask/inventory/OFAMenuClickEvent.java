package net.honormc.ofask.inventory;

import net.honormc.oneforall.inventory.InventoryClick;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;

/**
 * Fired via {@link net.minestom.server.event.EventDispatcher} whenever a player clicks a
 * slot in an {@link net.honormc.oneforall.inventory.OFAInventory} created through this
 * addon's Skript syntax. {@link OFAMenuClickWrapper} exposes it to Skript scripts as
 * {@code on ofa menu click}.
 *
 * <p>Cancellation writes straight through to the underlying {@link InventoryClick}, so
 * Skript's own generic {@code cancel event} effect works here for free.
 */
public class OFAMenuClickEvent implements PlayerEvent, CancellableEvent {

    private final InventoryClick click;
    private final Player player;

    public OFAMenuClickEvent(InventoryClick click, Player player) {
        this.click = click;
        this.player = player;
    }

    public InventoryClick click() {
        return click;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return click.cancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        click.cancelled(cancel);
    }
}
