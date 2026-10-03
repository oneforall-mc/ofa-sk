package net.honormc.ofask.profile;

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
import net.honormc.oneforall.profile.Profile;
import net.honormc.oneforall.profile.ProfileField;
import net.minestom.server.entity.Player;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("OFA Field")
@Description({
        "A saved per-player value: one you registered, or a built-in one like \"coins\", \"gems\", \"kills\" or \"deaths\".",
        "Saved automatically and follows the player between servers. Adding to or removing from a whole-number field",
        "is safe even when several servers change it at once."
})
@Examples({
        "add 1 to ofa field \"lobby:visits\" of player",
        "send \"Coins: %ofa field \"coins\" of player%\" to player",
        "reset ofa field \"lobby:last-server\" of player"
})
public class ExprOFAField extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprOFAField.class, Object.class, ExpressionType.PROPERTY,
                "[the] ofa field %string% of %players%",
                "%players%'[s] ofa field %string%");
    }

    private Expression<String> key;
    private Expression<Player> players;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        key = (Expression<String>) exprs[matchedPattern == 0 ? 0 : 1];
        players = (Expression<Player>) exprs[matchedPattern == 0 ? 1 : 0];
        return true;
    }

    private @Nullable ProfileField<?> field(Event event) {
        String name = key.getSingle(event);
        if (name == null) return null;
        ProfileField<?> field = ScriptFields.field(name);
        if (field == null) {
            Skript.warning("No OFA field called \"" + name + "\". Register it in 'on load' with: register ofa number field \"" + name + "\"");
        }
        return field;
    }

    @Override
    protected Object @Nullable [] get(Event event) {
        ProfileField<?> field = field(event);
        if (field == null) return null;
        List<Object> values = new ArrayList<>();
        for (Player player : players.getArray(event)) {
            Profile profile = ScriptFields.profile(player);
            if (profile == null) continue;
            Object value = profile.get(field);
            if (value != null) values.add(value);
        }
        return values.toArray();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET -> new Class[]{Object.class};
            case ADD, REMOVE -> new Class[]{Number.class};
            case RESET, DELETE -> new Class[0];
            default -> null;
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        ProfileField field = field(event);
        if (field == null) return;
        for (Player player : players.getArray(event)) {
            Profile profile = ScriptFields.profile(player);
            if (profile == null) continue;
            switch (mode) {
                case SET -> {
                    if (delta == null || delta.length == 0) continue;
                    Object value = ScriptFields.coerce(field, delta[0]);
                    if (value == null) {
                        Skript.warning("Can't store " + delta[0] + " in OFA field \"" + field.key() + "\"");
                        return;
                    }
                    profile.set(field, value);
                }
                case ADD, REMOVE -> {
                    if (delta == null || delta.length == 0) continue;
                    double amount = ((Number) delta[0]).doubleValue() * (mode == ChangeMode.REMOVE ? -1 : 1);
                    if (ScriptFields.isWholeNumber(field) && field.persistent()) {
                        // Atomic on Redis: concurrent adds from several servers all count.
                        profile.increment(field, Math.round(amount));
                    } else if (profile.get(field) instanceof Number current) {
                        profile.set(field, ScriptFields.coerce(field, current.doubleValue() + amount));
                    } else {
                        Skript.warning("Can't add to OFA field \"" + field.key() + "\": it doesn't hold a number");
                        return;
                    }
                }
                case RESET, DELETE -> profile.set(field, null);
                default -> {
                }
            }
        }
    }

    @Override
    public boolean isSingle() {
        return players.isSingle();
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "ofa field " + key.toString(event, debug) + " of " + players.toString(event, debug);
    }
}
