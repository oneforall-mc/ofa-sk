package net.honormc.ofask.inventory;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.inventory.BorderAnimatedInventory;
import net.honormc.oneforall.inventory.OFAInventory;
import net.honormc.oneforall.inventory.OFAInventoryType;
import net.honormc.oneforall.minestom.player.MinestomPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

/**
 * Creates an {@link OFAInventory} (or a {@link BorderAnimatedInventory} when "bordered" is
 * present) and wires its click/close callbacks to fire {@link OFAMenuClickEvent}/
 * {@link OFAMenuCloseEvent}, wrapped and handed straight to Skript's own Bukkit-shim event
 * bus, so every menu made through this pack is reachable from
 * {@code on ofa menu click}/{@code on ofa menu close} for free.
 *
 * <p>This deliberately bypasses skript-minestom's usual "fire a real Minestom event and let
 * {@code SkriptMinestom.initSkript}'s reflective wiring catch it" path — that path only
 * works for events that actually travel through Minestom's own {@code EventDispatcher}/node
 * tree, and an OFA menu click is a plain Java callback with no such event backing it. Calling
 * {@link Bukkit#getPluginManager()}{@code .callEvent(...)} directly is exactly what that
 * reflective wiring does on the last line anyway, so this is the same delivery mechanism
 * without the indirection (and without depending on which {@code EventNode} the host server
 * booted Skript on).
 */
@Name("New OFA Menu")
@Description("Creates a new oneforall-api menu, optionally with an animated border, with the given number of rows and title.")
@Examples("set {_menu} to a bordered 6 row ofa menu named \"&8Bounties\"")
@Since("1.0")
public class ExprOFAMenu extends SimpleExpression<OFAInventory> {

    static {
        Skript.registerExpression(ExprOFAMenu.class, OFAInventory.class, ExpressionType.COMBINED,
                "a bordered %integer% row ofa menu named %string%",
                "a[n] %integer% row ofa menu named %string%",
                // Pre-rename forms, kept for one release so existing scripts keep parsing.
                "a bordered %integer% row inventory named %string%",
                "a[n] %integer% row inventory named %string%");
    }

    private boolean bordered;
    private Expression<Integer> rows;
    private Expression<String> title;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        bordered = matchedPattern % 2 == 0;
        rows = (Expression<Integer>) exprs[0];
        title = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    protected OFAInventory @Nullable [] get(Event event) {
        Integer rowCount = rows.getSingle(event);
        String menuTitle = title.getSingle(event);
        if (rowCount == null || menuTitle == null) {
            Skript.warning("a [bordered] ... row ofa menu named ...: row count or title was null — no menu created.");
            return null;
        }
        int clampedRows = Math.max(1, Math.min(6, rowCount));
        OFAInventoryType type = OFAInventoryType.chest(clampedRows);
        OFAInventory menu = bordered
                ? new BorderAnimatedInventory(type, menuTitle)
                : new OFAInventory(type, menuTitle);
        wireEvents(menu);
        return new OFAInventory[]{menu};
    }

    private static void wireEvents(OFAInventory menu) {
        menu.onClick(click -> {
            if (click.player() instanceof MinestomPlayer mp) {
                Bukkit.getPluginManager().callEvent(new OFAMenuClickWrapper(new OFAMenuClickEvent(click, mp.handle())));
            }
        });
        menu.onClose(player -> {
            if (player instanceof MinestomPlayer mp) {
                Bukkit.getPluginManager().callEvent(new OFAMenuCloseWrapper(new OFAMenuCloseEvent(menu, mp.handle())));
            }
        });
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends OFAInventory> getReturnType() {
        return OFAInventory.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (bordered ? "a bordered " : "a ") + rows.toString(event, debug) + " row ofa menu named " + title.toString(event, debug);
    }
}
