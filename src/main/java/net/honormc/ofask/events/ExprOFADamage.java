package net.honormc.ofask.events;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.event.player.DamageCause;
import net.honormc.oneforall.event.player.PlayerEnvironmentalDamageEvent;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * {@code ofa damage cause} (text: fall, void, fire, lava, drowning, …; a stalagmite landing reads
 * as "fall") and {@code ofa damage} (the amount, changeable) in an environmental damage event.
 */
@Name("OFA Damage / Damage Cause")
@Description({
        "In an 'on ofa environmental damage' event: 'ofa damage cause' is what hurt the player, as text",
        "(fall, void, fire, lava, drowning, suffocation, starvation, explosion, magic, lightning, freeze, cactus,",
        "fly into wall, generic, other). 'ofa damage' is how much damage they take, and can be set, added to or reduced."
})
@Examples({
        "on ofa environmental damage:",
        "\tif ofa damage cause is \"fall\":",
        "\t\tset ofa damage to ofa damage / 2"
})
public class ExprOFADamage extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprOFADamage.class, Object.class, ExpressionType.SIMPLE,
                "[the] ofa damage cause",
                "[the] ofa damage [amount]");
    }

    private boolean cause;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        cause = matchedPattern == 0;
        if (!getParser().isCurrentEvent(OFAEventWrappers.EnvironmentalDamage.class)) {
            Skript.error("'" + this + "' can only be used in an 'on ofa environmental damage' event");
            return false;
        }
        return true;
    }

    private static @Nullable PlayerEnvironmentalDamageEvent damage(Event event) {
        return event instanceof OFAEventWrappers.EnvironmentalDamage w
                && w.getEvent().ofa() instanceof PlayerEnvironmentalDamageEvent e ? e : null;
    }

    @Override
    protected Object @Nullable [] get(Event event) {
        PlayerEnvironmentalDamageEvent e = damage(event);
        if (e == null) return null;
        if (cause) {
            DamageCause c = e.cause().isFall() ? DamageCause.FALL : e.cause();
            return new String[]{c.name().toLowerCase(Locale.ROOT).replace('_', ' ')};
        }
        return new Number[]{e.damage()};
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (cause) return null;
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        PlayerEnvironmentalDamageEvent e = damage(event);
        if (e == null || delta == null || delta.length == 0) return;
        double amount = ((Number) delta[0]).doubleValue();
        double next = switch (mode) {
            case ADD -> e.damage() + amount;
            case REMOVE -> e.damage() - amount;
            default -> amount;
        };
        e.damage(Math.max(0, next));
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return cause ? String.class : Number.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return cause ? "ofa damage cause" : "ofa damage";
    }
}
