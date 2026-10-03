package net.honormc.ofask.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

/** {@code on ofa status effect apply [of "ofa:combat_tag"]} (cancellable) / {@code ... remove}. */
public class EvtOFAStatusEffect extends SkriptEvent {

    static {
        Skript.registerEvent("OFA Status Effect Apply/Remove", EvtOFAStatusEffect.class,
                        new Class[]{OFAEventWrappers.StatusEffectApply.class, OFAEventWrappers.StatusEffectRemove.class},
                        "ofa status effect apply [of %-strings%]",
                        "ofa status effect remove [of %-strings%]")
                .description("Called when a OneForAll status effect is applied to (cancellable) or removed from a player.",
                        "'ofa status effect' is the effect's id.")
                .examples("on ofa status effect apply of \"ofa:combat_tag\":", "\tsend \"You are in combat!\" to player");
    }

    private boolean apply;
    private @Nullable Literal<String> ids;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
        apply = matchedPattern == 0;
        ids = (Literal<String>) args[0];
        return true;
    }

    @Override
    public boolean check(Event event) {
        boolean isApply = event instanceof OFAEventWrappers.StatusEffectApply;
        if (isApply != apply) return false;
        String id = ExprOFAEventValue.statusEffectId(event);
        if (id == null) return false;
        if (ids == null) return true;
        for (String wanted : ids.getAll()) {
            if (wanted.equalsIgnoreCase(id)) return true;
        }
        return false;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "ofa status effect " + (apply ? "apply" : "remove") + (ids == null ? "" : " of " + ids.toString(event, debug));
    }
}
