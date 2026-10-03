package net.honormc.ofask.inventory;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

/** Valid only inside {@link EvtMenuClick} — the slot the player just clicked. */
@Name("Clicked Slot")
@Description("The slot number clicked in an 'on ofa menu click' event.")
@Examples({
        "on ofa menu click:",
        "\tif clicked slot is 13:",
        "\t\tsend \"you clicked the diamond!\" to player"
})
public class ExprMenuEventSlot extends SimpleExpression<Integer> {

    static {
        Skript.registerExpression(ExprMenuEventSlot.class, Integer.class, ExpressionType.SIMPLE, "[the] clicked slot");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        return getParser().isCurrentEvent(OFAMenuClickWrapper.class);
    }

    @Override
    protected Integer @Nullable [] get(Event event) {
        if (!(event instanceof OFAMenuClickWrapper wrapper)) {
            return null;
        }
        return new Integer[]{wrapper.getEvent().click().slot()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "clicked slot";
    }
}
