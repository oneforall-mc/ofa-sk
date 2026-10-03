package net.honormc.ofask.inventory;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.OneForAll;
import net.honormc.oneforall.inventory.OFAInventory;
import net.honormc.oneforall.player.OFAPlayer;
import net.minestom.server.entity.Player;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

@Name("Open OFA Menu")
@Description("Opens an oneforall-api menu for a player.")
@Examples("open ofa menu {_menu} for player")
public class EffMenuOpen extends Effect {

    static {
        // "open %ofainventory% for %players%" would textually collide with the core
        // "open %inventory/inventorytype% for %players%" effect — {_menu} is an untyped
        // local variable, so the core effect's init() also succeeds for it, and being
        // registered first (core boots before addons) it wins the ambiguity and silently
        // no-ops since an OFAInventory is neither an Inventory nor an InventoryType. The
        // extra "ofa (menu|inventory)" literal keeps this pattern unambiguous.
        Skript.registerEffect(EffMenuOpen.class, "open [the] ofa (menu|inventory) %ofainventory% for %players%");
    }

    private Expression<OFAInventory> menu;
    private Expression<Player> players;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        menu = (Expression<OFAInventory>) exprs[0];
        players = (Expression<Player>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        OFAInventory ofaMenu = menu.getSingle(event);
        if (ofaMenu == null) {
            Skript.warning("open: menu expression was null — nothing to open.");
            return;
        }
        for (Player player : players.getArray(event)) {
            OFAPlayer ofaPlayer = OneForAll.players().get(player.getUuid());
            if (ofaPlayer == null) {
                Skript.warning("open: no OFAPlayer registered for " + player.getUsername()
                        + " (" + player.getUuid() + ") — menu not opened.");
                continue;
            }
            ofaMenu.open(ofaPlayer);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "open ofa menu " + menu.toString(event, debug) + " for " + players.toString(event, debug);
    }
}
