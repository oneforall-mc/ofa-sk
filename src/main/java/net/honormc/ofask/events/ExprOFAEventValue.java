package net.honormc.ofask.events;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.event.player.PlayerChatEvent;
import net.honormc.oneforall.event.region.BoundEnterEvent;
import net.honormc.oneforall.event.region.BoundExitEvent;
import net.honormc.oneforall.event.statuseffect.StatusEffectApplyEvent;
import net.honormc.oneforall.event.statuseffect.StatusEffectRemoveEvent;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

/** The text values of OFA events: the region id, the status effect id, and the chat message. */
@Name("OFA Event Values")
@Description({
        "'ofa region': the region id in an 'on ofa region enter/exit' event.",
        "'ofa status effect': the effect id in an 'on ofa status effect apply/remove' event.",
        "'ofa chat message': what the player typed, in an 'on ofa chat' event."
})
@Examples({
        "on ofa region enter:",
        "\tsend \"Entering %ofa region%\" to player",
        "on ofa chat:",
        "\tif ofa chat message contains \"discord\":",
        "\t\tsend \"Join our Discord!\" to player"
})
public class ExprOFAEventValue extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprOFAEventValue.class, String.class, ExpressionType.SIMPLE,
                "[the] [event-]ofa region",
                "[the] [event-]ofa status effect",
                "[the] ofa chat message");
    }

    private int which;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        which = matchedPattern;
        boolean valid = switch (which) {
            case 0 -> getParser().isCurrentEvent(OFAEventWrappers.RegionEnter.class, OFAEventWrappers.RegionExit.class);
            case 1 -> getParser().isCurrentEvent(OFAEventWrappers.StatusEffectApply.class, OFAEventWrappers.StatusEffectRemove.class);
            default -> getParser().isCurrentEvent(OFAEventWrappers.Chat.class);
        };
        if (!valid) {
            Skript.error(switch (which) {
                case 0 -> "'ofa region' can only be used in an 'on ofa region enter/exit' event";
                case 1 -> "'ofa status effect' can only be used in an 'on ofa status effect apply/remove' event";
                default -> "'ofa chat message' can only be used in an 'on ofa chat' event";
            });
        }
        return valid;
    }

    @Override
    protected String @Nullable [] get(Event event) {
        String value = switch (which) {
            case 0 -> regionId(event);
            case 1 -> statusEffectId(event);
            default -> event instanceof OFAEventWrappers.Chat w && w.getEvent().ofa() instanceof PlayerChatEvent e ? e.message() : null;
        };
        return value == null ? null : new String[]{value};
    }

    static @Nullable String regionId(Event event) {
        if (event instanceof OFAEventWrappers.RegionEnter w && w.getEvent().ofa() instanceof BoundEnterEvent e) return e.bound().id();
        if (event instanceof OFAEventWrappers.RegionExit w && w.getEvent().ofa() instanceof BoundExitEvent e) return e.bound().id();
        return null;
    }

    static @Nullable String statusEffectId(Event event) {
        if (event instanceof OFAEventWrappers.StatusEffectApply w && w.getEvent().ofa() instanceof StatusEffectApplyEvent e) {
            return e.definition().id();
        }
        if (event instanceof OFAEventWrappers.StatusEffectRemove w && w.getEvent().ofa() instanceof StatusEffectRemoveEvent e) {
            return e.definition().id();
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return switch (which) {
            case 0 -> "ofa region";
            case 1 -> "ofa status effect";
            default -> "ofa chat message";
        };
    }
}
