package net.honormc.ofask.inventory;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Item;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.inventory.OFAInventory;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

@Name("Fill Empty OFA Menu Slots")
@Description("Fills every empty slot of an oneforall-api menu with an item — a classic menu background.")
@Examples("fill empty slots of ofa menu {_menu} with gray stained glass pane named \" \"")
public class EffMenuFillEmpty extends Effect {

    static {
        // No core effect starts with "fill %inventories% with ...", so this is already
        // unambiguous — the "ofa" literal is added anyway for consistency with the rest
        // of this pack.
        Skript.registerEffect(EffMenuFillEmpty.class,
                "fill [the] empty slots of ofa menu %ofainventory% with %item%",
                "fill [the] empty ofa slots of %ofainventory% with %item%"); // pre-rename form
    }

    private Expression<OFAInventory> menu;
    private Expression<Item> item;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        menu = (Expression<OFAInventory>) exprs[0];
        item = (Expression<Item>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        OFAInventory ofaMenu = menu.getSingle(event);
        Item skriptItem = item.getSingle(event);
        if (ofaMenu == null || skriptItem == null) {
            Skript.warning("fill the empty slots of ... with ...: menu or item was null — nothing filled.");
            return;
        }
        ofaMenu.fillEmpty(SkriptGuiItems.toGuiItem(skriptItem.getItem()));
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "fill empty slots of ofa menu " + menu.toString(event, debug) + " with " + item.toString(event, debug);
    }
}
