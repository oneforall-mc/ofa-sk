package net.honormc.ofask.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

/** {@code on ofa region enter [of "spawn"]} / {@code on ofa region exit [of "spawn"]}. Cancellable. */
public class EvtOFARegion extends SkriptEvent {

    static {
        Skript.registerEvent("OFA Region Enter/Exit", EvtOFARegion.class,
                        new Class[]{OFAEventWrappers.RegionEnter.class, OFAEventWrappers.RegionExit.class},
                        "ofa region enter [of %-strings%]",
                        "ofa region exit [of %-strings%]")
                .description("Called when a player walks into or out of a region. Cancelling stops the move.",
                        "Without 'of' it fires for every region; 'ofa region' is the region's id.")
                .examples("on ofa region enter of \"spawn\":", "\tsend \"Welcome to spawn\" to player");
    }

    private boolean enter;
    private @Nullable Literal<String> ids;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
        enter = matchedPattern == 0;
        ids = (Literal<String>) args[0];
        return true;
    }

    @Override
    public boolean check(Event event) {
        boolean isEnter = event instanceof OFAEventWrappers.RegionEnter;
        if (isEnter != enter) return false;
        String id = ExprOFAEventValue.regionId(event);
        if (id == null) return false;
        if (ids == null) return true;
        for (String wanted : ids.getAll()) {
            if (wanted.equalsIgnoreCase(id)) return true;
        }
        return false;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "ofa region " + (enter ? "enter" : "exit") + (ids == null ? "" : " of " + ids.toString(event, debug));
    }
}
