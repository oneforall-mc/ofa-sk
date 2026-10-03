package net.honormc.ofask.inventory;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.inventory.BorderAnimatedInventory;
import net.honormc.oneforall.inventory.OFAInventory;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

@Name("Set OFA Menu Border Color")
@Description("Sets the animated border colour of a bordered oneforall-api menu. No-op with a warning if the menu wasn't created with 'bordered'.")
@Examples("set border colour of ofa menu {_menu} to \"aqua\"")
public class EffMenuBorderColor extends Effect {

    static {
        // No core property expression exists for "border color of X", so this pattern
        // can't be reached through core's generic "set %~objects% to %objects%" — the
        // "ofa" literal is added anyway for consistency with the rest of this pack.
        Skript.registerEffect(EffMenuBorderColor.class,
                "set [the] border colo[u]r of ofa menu %ofainventory% to %string%",
                "set the ofa border colo[u]r of %ofainventory% to %string%"); // pre-rename form
    }

    private Expression<OFAInventory> menu;
    private Expression<String> colour;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        menu = (Expression<OFAInventory>) exprs[0];
        colour = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        OFAInventory ofaMenu = menu.getSingle(event);
        String colourName = colour.getSingle(event);
        if (ofaMenu == null || colourName == null) {
            return;
        }
        if (!(ofaMenu instanceof BorderAnimatedInventory bordered)) {
            Skript.warning("Can't set the border color of a menu that wasn't created with 'bordered' — ignoring.");
            return;
        }
        bordered.borderColor(colourName);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "set border colour of ofa menu " + menu.toString(event, debug) + " to " + colour.toString(event, debug);
    }
}
