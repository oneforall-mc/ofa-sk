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

@Name("Set OFA Menu Slot")
@Description("Sets the item in a slot of an oneforall-api menu.")
@Examples("set slot 13 of ofa menu {_menu} to diamond named \"&bClick me\"")
public class EffMenuSetItem extends Effect {

    static {
        // A bare "set slot %integer% of %ofainventory% to %item%" would textually collide with
        // core's generic "set %~objects% to %objects%" (EffChange) combined with core's
        // "slot[s] %integers% of %inventories%" (ExprSlot) — {_menu} is an untyped local
        // variable, so both core patterns also succeed against it, and core wins the
        // ambiguity (registered before addons), silently no-op'ing since our OFAInventory
        // isn't a real AbstractInventory. "ofa menu" in front of the menu keeps it
        // unambiguous: ExprSlot's %inventories% can't parse "ofa menu {_menu}".
        Skript.registerEffect(EffMenuSetItem.class,
                "set slot %integer% of ofa menu %ofainventory% to %item%",
                "set ofa slot %integer% of %ofainventory% to %item%"); // pre-rename form
    }

    private Expression<Integer> slot;
    private Expression<OFAInventory> menu;
    private Expression<Item> item;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        slot = (Expression<Integer>) exprs[0];
        menu = (Expression<OFAInventory>) exprs[1];
        item = (Expression<Item>) exprs[2];
        return true;
    }

    @Override
    protected void execute(Event event) {
        Integer slotIndex = slot.getSingle(event);
        OFAInventory ofaMenu = menu.getSingle(event);
        Item skriptItem = item.getSingle(event);
        if (slotIndex == null || ofaMenu == null || skriptItem == null) {
            Skript.warning("set slot ... of ...: slot, menu or item was null — nothing set.");
            return;
        }
        ofaMenu.setItem(slotIndex, SkriptGuiItems.toGuiItem(skriptItem.getItem()));
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "set slot " + slot.toString(event, debug) + " of ofa menu " + menu.toString(event, debug) + " to " + item.toString(event, debug);
    }
}
