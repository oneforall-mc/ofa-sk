package net.honormc.ofask.inventory;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

@Name("OFA Menu Close")
@Description("Called when a player closes an oneforall-api menu created by this addon.")
@Examples("on ofa menu close:")
public class EvtMenuClose extends SkriptEvent {

    static {
        Skript.registerEvent("OFA Menu Close", EvtMenuClose.class, OFAMenuCloseWrapper.class,
                "ofa menu close", "ofa inventory close"); // second is the pre-rename form
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(Event event) {
        return true;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "ofa menu close";
    }
}
